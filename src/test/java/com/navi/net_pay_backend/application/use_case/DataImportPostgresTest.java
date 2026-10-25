package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.application.model.ImportResult;
import com.navi.net_pay_backend.application.model.ImportRow;
import com.navi.net_pay_backend.infrastructure.service.SpreadsheetImportReader;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Explicitly enabled smoke test: retains the example records in the configured PostgreSQL. */
@SpringBootTest(properties = "spring.jpa.show-sql=false")
@EnabledIfSystemProperty(named = "netpay.import.smoke", matches = "true")
class DataImportPostgresTest {
    private static final Logger log = LoggerFactory.getLogger(DataImportPostgresTest.class);

    @Autowired private DataImportUseCase importer;
    @Autowired private SpreadsheetImportReader reader;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void validatesRollbackAndLoadsExamplesIntoPostgres() throws Exception {
        List<ImportRow> plans = example("services.csv");
        List<ImportRow> clients = example("clients.csv");
        List<ImportRow> missingPlans = plans.stream().filter(row -> jdbc.queryForObject(
                "select count(*) from app_service where lower(name) = lower(?) and deleted_at is null",
                Long.class, row.values().get("name")) == 0).toList();
        List<ImportRow> missingClients = clients.stream().filter(row -> jdbc.queryForObject(
                "select count(*) from client where business_name = ? and contact_name = ? and deleted_at is null",
                Long.class, row.values().get("businessName"), row.values().get("contactName")) == 0).toList();

        if (!missingPlans.isEmpty()) {
            long before = count("app_service");
            long auditBefore = count("audit_log");
            valid(importer.execute("services", missingPlans, true), missingPlans.size());
            assertEquals(before, count("app_service"), "Plan dry-run must roll back all inserts");
            assertEquals(auditBefore, count("audit_log"), "Dry-run must roll back audit records");
            valid(importer.execute("services", missingPlans, false), missingPlans.size());
            assertEquals(before + missingPlans.size(), count("app_service"));
            assertEquals(auditBefore + missingPlans.size(), count("audit_log"));
        }

        long before = count("client");
        long auditBefore = count("audit_log");
        valid(importer.execute("clients", clients, true), 60);
        // Exercise the Excel reader and real persistence path as well as the CSV examples.
        valid(importer.execute("clients", asExcel(clients), true), 60);
        ImportResult invalid = importer.execute("clients", example("invalid-clients.csv"), true);
        assertEquals(5, invalid.failed(), invalid.toString());
        assertEquals(1, invalid.successful(), invalid.toString());
        assertEquals(before, count("client"), "Client previews must not leave records behind");
        assertEquals(auditBefore, count("audit_log"));

        if (!missingClients.isEmpty()) {
            valid(importer.execute("clients", missingClients, false), missingClients.size());
        }
        assertEquals(before + missingClients.size(), count("client"));
        assertEquals(auditBefore + missingClients.size(), count("audit_log"));

        var nullable = jdbc.queryForMap("""
                select contact_email, contact_phone, address, latitude, longitude, collector_id
                from client where business_name = 'Import Test Client 009' and deleted_at is null
                order by id desc limit 1
                """);
        assertTrue(nullable.values().stream().allMatch(java.util.Objects::isNull), nullable.toString());
        assertEquals(60L, jdbc.queryForObject("""
                select count(distinct business_name) from client
                where business_name ~ '^Import Test Client [0-9]{3}$' and deleted_at is null
                """, Long.class));
        log.info("PostgreSQL import verified: {} plans and {} clients created; {} plans and {} clients already existed. "
                        + "CSV/XLSX dry-run rollback, null persistence and 5 invalid rows verified.",
                missingPlans.size(), missingClients.size(), 10 - missingPlans.size(), 60 - missingClients.size());
    }

    private void valid(ImportResult result, int expected) {
        assertEquals(0, result.failed(), result.toString());
        assertEquals(expected, result.successful(), result.toString());
    }

    private long count(String table) {
        return jdbc.queryForObject("select count(*) from " + table, Long.class);
    }

    private List<ImportRow> example(String filename) throws Exception {
        try (var input = com.navi.net_pay_backend.support.ImportFixtures.input(filename)) {
            return reader.read(input, filename, ',');
        }
    }

    private List<ImportRow> asExcel(List<ImportRow> rows) throws Exception {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Clientes");
            var headers = new ArrayList<>(rows.get(0).values().keySet());
            var header = sheet.createRow(0);
            for (int col = 0; col < headers.size(); col++) header.createCell(col).setCellValue(headers.get(col));
            for (int index = 0; index < rows.size(); index++) {
                var row = sheet.createRow(index + 1);
                for (int col = 0; col < headers.size(); col++) {
                    String value = rows.get(index).values().get(headers.get(col));
                    if (value != null) row.createCell(col).setCellValue(value);
                }
            }
            workbook.write(output);
            return reader.read(new ByteArrayInputStream(output.toByteArray()), "clients.xlsx", ',');
        }
    }
}
