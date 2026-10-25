package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.application.model.ImportRow;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppPayment;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.repository.AppPaymentRepository;
import com.navi.net_pay_backend.domain.service.PasswordHashService;
import com.navi.net_pay_backend.domain.service.ReceiptStorageService;
import com.navi.net_pay_backend.infrastructure.service.SpreadsheetImportReader;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "spring.jpa.show-sql=false")
@Transactional
class UserPaymentImportIntegrationTest {
    @Autowired private UserPaymentImportHandler handler;
    @Autowired private ImportPartyResolver resolver;
    @Autowired private DataImportUseCase importer;
    @Autowired private AppPaymentUseCase paymentUseCase;
    @Autowired private AppPaymentRepository payments;
    @Autowired private PasswordHashService passwords;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private EntityManager entityManager;
    @Autowired private SpreadsheetImportReader reader;
    @MockitoBean private ReceiptStorageService storage;

    private final List<String> importedKeys = new ArrayList<>();
    private final List<String> importedEmails = new ArrayList<>();

    @Test
    void generatedUserCsvAndPaymentExcelWorkWithRealPersistence() throws Exception {
        String prefix = UUID.randomUUID().toString().substring(0, 8) + "-";
        for (ImportRow user : example("users.csv")) {
            var values = new LinkedHashMap<>(user.values());
            values.put("fullName", prefix + values.get("fullName"));
            values.put("email", prefix + values.get("email"));
            importedEmails.add(values.get("email"));
            handler.user(values);
        }
        long service = service();
        long freeService = service();
        jdbc.update("update app_service set monthly_price = 0 where id = ?", freeService);
        for (ImportRow client : example("clients.csv").subList(0, 20)) {
            var values = client.values();
            boolean free = values.get("serviceName").equals("Test Plan 9");
            client(prefix + values.get("businessName"), prefixed(prefix, values.get("contactEmail")),
                    prefixed(prefix, values.get("contactPhone")), free ? freeService : service);
        }
        List<ImportRow> paymentRows = new ArrayList<>();
        for (ImportRow payment : example("payments.csv")) {
            var values = new LinkedHashMap<>(payment.values());
            for (String field : List.of("importKey", "clientName", "clientEmail", "clientPhone", "collectorName", "collectorEmail")) {
                values.put(field, prefixed(prefix, values.get(field)));
            }
            importedKeys.add(values.get("importKey"));
            paymentRows.add(new ImportRow(payment.row(), values));
        }
        // Exercise .xlsx with all twenty rows, including name/email/phone selectors, enums and numeric IDs.
        for (ImportRow row : asExcel(paymentRows)) handler.payment(row.values(), true);
        assertEquals(20L, jdbc.queryForObject("select count(*) from payment where import_key like ?", Long.class, prefix + "%"));
        assertEquals(18L, jdbc.queryForObject("""
                select count(*) from receipt r join payment p on p.id = r.payment_id where p.import_key like ?
                """, Long.class, prefix + "%"));
        assertEquals(0L, jdbc.queryForObject("""
                select count(*) from payment_allocation a join payment p on p.id = a.payment_id
                join client c on c.id = p.client_id join app_service s on s.id = c.service_id
                where p.import_key like ? and s.monthly_price = 0
                """, Long.class, prefix + "%"));
        verifyNoInteractions(storage);
    }

    @Test
    void usersNormalizeEmailsHashPasswordsAndRejectDuplicatesIgnoringCase() {
        String email = UUID.randomUUID() + "@EXAMPLE.COM";
        String password = UUID.randomUUID().toString();
        var row = new LinkedHashMap<>(Map.of("fullName", "Import User", "email", email, "password", password,
                "role", "collector", "status", "active"));
        String hashId = handler.user(row);
        importedEmails.add(email.toLowerCase(Locale.ROOT));
        var saved = jdbc.queryForMap("select email, password_hash from app_user where hash_id = ?", hashId);
        assertEquals(email.toLowerCase(Locale.ROOT), saved.get("email"));
        assertNotEquals(password, saved.get("password_hash"));
        assertTrue(passwords.compare(password, (String) saved.get("password_hash")));
        error("import_user_email_already_exists", () -> handler.user(row));

        String generatedEmail = UUID.randomUUID() + "@example.com";
        importedEmails.add(generatedEmail);
        String generatedHashId = handler.user(Map.of("fullName", "Generated Password", "email", generatedEmail));
        var defaults = jdbc.queryForMap("""
                select u.password_hash, role.internal_id as role, status.internal_id as status
                from app_user u join adm_typology role on role.typology_id = u.tp_role
                join adm_typology status on status.typology_id = u.tp_status where u.hash_id = ?
                """, generatedHashId);
        assertTrue(((String) defaults.get("password_hash")).startsWith("$2"));
        assertEquals(62L, ((Number) defaults.get("role")).longValue());
        assertEquals(49L, ((Number) defaults.get("status")).longValue());
    }

    @Test
    void completedPaymentsResolveReadableReferencesAndCreateAllocationsReceiptsAndAudit() {
        String name = "Import Client " + UUID.randomUUID();
        String collectorEmail = UUID.randomUUID() + "@example.com";
        long service = service();
        long collector = user("Import Collector", collectorEmail, 62, 49);
        long client = client(name, UUID.randomUUID() + "@example.com", "001234", service);
        var row = payment(name.toUpperCase(Locale.ROOT), "150.00");
        row.put("collectorEmail", collectorEmail.toUpperCase(Locale.ROOT));
        row.put("method", "TRANSFER");
        row.put("reference", "Statement 1");
        String hashId = handler.payment(row, false);
        long id = paymentId(hashId);
        var saved = jdbc.queryForMap("select client_id, collector_id, import_key, reference from payment where id = ?", id);
        assertEquals(client, ((Number) saved.get("client_id")).longValue());
        assertEquals(collector, ((Number) saved.get("collector_id")).longValue());
        assertEquals(row.get("importKey"), saved.get("import_key"));
        assertEquals("Statement 1", saved.get("reference"));
        assertEquals(new BigDecimal("150.00"), jdbc.queryForObject(
                "select sum(amount) from payment_allocation where payment_id = ?", BigDecimal.class, id));
        assertEquals(2L, jdbc.queryForObject("select count(*) from payment_allocation where payment_id = ?", Long.class, id));
        assertEquals(1L, jdbc.queryForObject("select count(*) from receipt where payment_id = ?", Long.class, id));
        assertEquals(1L, jdbc.queryForObject("select count(*) from audit_log where entity_hash_id = ? and action = 'payment_created'",
                Long.class, hashId));
        assertTrue(payments.existsByImportKey(row.get("importKey")));
        error("import_payment_already_imported", () -> handler.payment(row, false));

        paymentUseCase.delete(hashId);
        entityManager.flush();
        assertTrue(payments.existsByImportKey(row.get("importKey")), "An annulled payment still reserves its import key");
        error("import_payment_already_imported", () -> handler.payment(row, false));
    }

    @Test
    void pendingAndFailedPaymentsDoNotPayDebtAndCompletionPreservesImportKey() {
        String name = "Pending Client " + UUID.randomUUID();
        client(name, null, "009900", service());
        for (String status : List.of("PENDING", "FAILED")) {
            var row = payment(name, "50.00");
            row.put("status", status);
            String hashId = handler.payment(row, false);
            long id = paymentId(hashId);
            assertEquals(0L, jdbc.queryForObject("select count(*) from payment_allocation where payment_id = ?", Long.class, id));
            assertEquals(0L, jdbc.queryForObject("select count(*) from receipt where payment_id = ?", Long.class, id));
            if (status.equals("PENDING")) {
                paymentUseCase.update(hashId, AppPayment.builder()
                        .tpPaymentStatus(AdmTypology.builder().internalId(91L).build()).build());
                entityManager.flush();
                assertEquals(row.get("importKey"), jdbc.queryForObject("select import_key from payment where id = ?", String.class, id));
                assertEquals(new BigDecimal("50.00"), jdbc.queryForObject(
                        "select sum(amount) from payment_allocation where payment_id = ?", BigDecimal.class, id));
                assertEquals(1L, jdbc.queryForObject("select count(*) from receipt where payment_id = ?", Long.class, id));
            }
        }
    }

    @Test
    void ambiguousNamesRequireMoreDataAndCollectorsMustBeActiveWithCorrectRole() {
        String name = "Repeated Client " + UUID.randomUUID();
        long service = service();
        String email = UUID.randomUUID() + "@example.com";
        long expected = client(name, email, "000111", service);
        client(name, UUID.randomUUID() + "@example.com", "000222", service);
        error("import_client_ambiguous", () -> resolver.client(Map.of("clientName", name)));
        assertEquals(expected, resolver.client(Map.of("clientName", name, "clientEmail", email)).getId());
        assertEquals(expected, resolver.client(Map.of("clientPhone", "000111")).getId());
        error("import_client_not_found", () -> resolver.client(Map.of("clientName", name, "clientEmail", "missing@example.com")));

        String collectorName = "Repeated Collector " + UUID.randomUUID();
        String collectorEmail = UUID.randomUUID() + "@example.com";
        long collector = user(collectorName, collectorEmail, 62, 49);
        user(collectorName, UUID.randomUUID() + "@example.com", 62, 49);
        error("import_collector_ambiguous", () -> resolver.collector(Map.of("collectorName", collectorName)));
        assertEquals(collector, resolver.collector(Map.of("collectorName", collectorName, "collectorEmail", collectorEmail)).getId());
        String adminEmail = UUID.randomUUID() + "@example.com";
        user("Not a collector", adminEmail, 61, 49);
        error("import_collector_role_invalid", () -> resolver.collector(Map.of("collectorEmail", adminEmail)));
        String inactiveEmail = UUID.randomUUID() + "@example.com";
        user("Inactive collector", inactiveEmail, 62, 50);
        error("import_collector_inactive", () -> resolver.collector(Map.of("collectorEmail", inactiveEmail)));
        jdbc.update("update client set deleted_at = CURRENT_TIMESTAMP where id = ?", expected);
        error("import_client_not_found", () -> resolver.client(Map.of("clientEmail", email)));
    }

    @Test
    void completedPreviewNeverCallsCloudStorageAndUserPreviewRollsBack() {
        String name = "Preview Client " + UUID.randomUUID();
        client(name, null, null, service());
        var row = payment(name, "25.00");
        String hashId = handler.payment(row, true);
        assertEquals(1L, jdbc.queryForObject("select count(*) from receipt where payment_id = ?", Long.class, paymentId(hashId)));
        verifyNoInteractions(storage);

        String email = UUID.randomUUID() + "@example.com";
        var result = importer.execute("users", List.of(new ImportRow(2, Map.of("fullName", "Preview User", "email", email))), true);
        assertEquals(1, result.successful(), result.toString());
        assertNull(result.rows().get(0).hashId());
        assertEquals(0L, jdbc.queryForObject("select count(*) from app_user where email = ?", Long.class, email));
    }

    @Test
    void uniqueIndexRejectsDuplicateImportKeysAtDatabaseLevel() {
        String name = "Unique Key Client " + UUID.randomUUID();
        client(name, null, null, service());
        var row = payment(name, "10.00");
        long id = paymentId(handler.payment(row, false));
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("""
                insert into payment (hash_id, import_key, client_id, service_id, amount, paid_at, tp_payment_method, tp_payment_status)
                select ?, import_key, client_id, service_id, amount, paid_at, tp_payment_method, tp_payment_status
                from payment where id = ?
                """, UUID.randomUUID().toString(), id));
    }

    @AfterTransaction
    void noTestPaymentsOrUsersRemainAfterRollback() {
        for (String key : importedKeys) {
            assertEquals(0L, jdbc.queryForObject("select count(*) from payment where import_key = ?", Long.class, key));
        }
        for (String email : importedEmails) {
            assertEquals(0L, jdbc.queryForObject("select count(*) from app_user where email = ?", Long.class, email));
        }
    }

    private Map<String, String> payment(String name, String amount) {
        String key = "test-import-" + UUID.randomUUID();
        importedKeys.add(key);
        return new LinkedHashMap<>(Map.of("importKey", key, "clientName", name, "amount", amount,
                "paidAt", LocalDate.now().toString(), "method", "CASH"));
    }

    private void error(String key, Runnable operation) {
        assertEquals(key, assertThrows(DomainException.class, operation::run).getMessage());
    }

    private long paymentId(String hashId) {
        return jdbc.queryForObject("select id from payment where hash_id = ?", Long.class, hashId);
    }

    private long user(String name, String email, long role, long status) {
        return jdbc.queryForObject("""
                insert into app_user (hash_id, full_name, email, password_hash, tp_role, tp_status)
                values (?, ?, ?, 'unused-test-hash',
                        (select typology_id from adm_typology where internal_id = ?),
                        (select typology_id from adm_typology where internal_id = ?)) returning id
                """, Long.class, UUID.randomUUID().toString(), name, email, role, status);
    }

    private long service() {
        return jdbc.queryForObject("""
                insert into app_service (hash_id, name, speed, monthly_price, tp_type, tp_status)
                values (?, ?, '10 Mbps', 100,
                        (select typology_id from adm_typology where internal_id = 71),
                        (select typology_id from adm_typology where internal_id = 49)) returning id
                """, Long.class, UUID.randomUUID().toString(), "Import Plan " + UUID.randomUUID());
    }

    private long client(String name, String email, String phone, long service) {
        return jdbc.queryForObject("""
                insert into client (hash_id, business_name, contact_name, contact_email, contact_phone, customer_since, service_id, tp_status)
                values (?, ?, 'Test Contact', ?, ?, ?, ?,
                        (select typology_id from adm_typology where internal_id = 49)) returning id
                """, Long.class, UUID.randomUUID().toString(), name, email, phone,
                YearMonth.now().minusMonths(2).atDay(1), service);
    }

    private String prefixed(String prefix, String value) {
        return value == null ? null : prefix + value;
    }

    private List<ImportRow> example(String filename) throws Exception {
        try (var input = com.navi.net_pay_backend.support.ImportFixtures.input(filename)) {
            return reader.read(input, filename, ',');
        }
    }

    private List<ImportRow> asExcel(List<ImportRow> rows) throws Exception {
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(); var output = new java.io.ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Pagos");
            var headers = new ArrayList<>(rows.get(0).values().keySet());
            var header = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) header.createCell(i).setCellValue(headers.get(i));
            for (int i = 0; i < rows.size(); i++) {
                var row = sheet.createRow(i + 1);
                for (int col = 0; col < headers.size(); col++) {
                    String value = rows.get(i).values().get(headers.get(col));
                    if (value != null) row.createCell(col).setCellValue(value);
                }
            }
            workbook.write(output);
            return reader.read(new java.io.ByteArrayInputStream(output.toByteArray()), "payments.xlsx", ',');
        }
    }
}
