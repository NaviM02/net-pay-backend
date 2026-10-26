package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.application.model.ImportRow;
import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.domain.repository.*;
import com.navi.net_pay_backend.domain.service.AuditLogService;
import com.navi.net_pay_backend.infrastructure.service.SpreadsheetImportReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class DataImportUseCaseTest {
    private DataImportUseCase importer;
    private AppClientRepository clients;
    private AppServiceRepository services;
    private PlatformTransactionManager transactions;
    private UserPaymentImportHandler userPaymentHandler;

    @BeforeEach
    void setUp() {
        clients = mock(AppClientRepository.class);
        services = mock(AppServiceRepository.class);
        var users = mock(AppUserRepository.class);
        var audit = mock(AuditLogService.class);
        transactions = mock(PlatformTransactionManager.class);
        when(transactions.getTransaction(any())).thenAnswer(call -> new SimpleTransactionStatus());
        when(clients.save(any())).thenAnswer(call -> call.getArgument(0));
        when(services.save(any())).thenAnswer(call -> call.getArgument(0));
        when(services.findByNameNotDeleted(anyString())).thenAnswer(call ->
                call.getArgument(0).equals("Plan inexistente") ? Optional.empty()
                        : Optional.of(AppService.builder().id(1L).name(call.getArgument(0)).build()));
        userPaymentHandler = mock(UserPaymentImportHandler.class);
        importer = new DataImportUseCase(new AppServiceUseCase(services, audit),
                new AppClientUseCase(clients, services, users, audit), services,
                new ImportPartyResolver(mock(ImportLookupRepository.class), users, mock(AccessControlRepository.class)), userPaymentHandler, transactions);
    }

    @Test
    void importsBothCompleteExamplesAndPreservesOptionalNulls() throws Exception {
        var plans = importer.execute("services", example("services.csv"), false);
        var result = importer.execute("clients", example("clients.csv"), false);
        assertEquals(10, plans.successful());
        assertEquals(60, result.successful());
        assertEquals(0, result.failed());
        assertTrue(result.rows().stream().allMatch(r -> r.hashId() != null && r.status().equals("CREATED")));
        var captured = org.mockito.ArgumentCaptor.forClass(com.navi.net_pay_backend.domain.entity.AppClient.class);
        verify(clients, times(60)).save(captured.capture());
        var ninth = captured.getAllValues().get(8);
        assertNull(ninth.getContactEmail());
        assertNull(ninth.getContactPhone());
        assertNull(ninth.getAddress());
        assertNull(ninth.getLatitude());
        assertNull(ninth.getCollector());
        assertNotNull(ninth.getCustomerSince());
    }

    @Test
    void dryRunRollsBackEveryRowAndDoesNotExposeTemporaryIds() throws Exception {
        var result = importer.execute("services", example("services.csv"), true);
        assertEquals(10, result.successful());
        assertTrue(result.rows().stream().allMatch(r -> r.hashId() == null && r.status().equals("VALID")));
        // TransactionTemplate delegates completion to the manager, which honors rollback-only.
        var statuses = org.mockito.ArgumentCaptor.forClass(org.springframework.transaction.TransactionStatus.class);
        verify(transactions, times(10)).commit(statuses.capture());
        assertTrue(statuses.getAllValues().stream().allMatch(org.springframework.transaction.TransactionStatus::isRollbackOnly));
    }

    @Test
    void invalidRowsDoNotPreventLaterValidRows() throws Exception {
        var result = importer.execute("clients", example("invalid-clients.csv"), false);
        assertEquals(5, result.failed());
        assertEquals(1, result.successful());
        assertEquals("CREATED", result.rows().get(5).status());
        verify(transactions, times(5)).rollback(any());
        verify(transactions).commit(any());
    }

    @Test
    void commitConstraintFailureIsIsolatedFromNextRow() throws Exception {
        doThrow(new DataIntegrityViolationException("private database details")).doNothing()
                .when(transactions).commit(any());
        var rows = example("services.csv").subList(0, 2);
        var result = importer.execute("services", rows, false);
        assertEquals(1, result.failed());
        assertEquals(1, result.successful());
        assertEquals("import_constraint_violation", result.rows().get(0).error());
    }

    @Test
    void existingClientNameIsReportedForThatRowAndDoesNotAbortFollowingRows() throws Exception {
        when(clients.existsByBusinessNameAndStatusNot(anyString(), anyLong()))
                .thenReturn(true)
                .thenReturn(false);
        var rows = example("clients.csv").subList(0, 2);

        var result = importer.execute("clients", rows, false);

        assertEquals(1, result.failed());
        assertEquals(1, result.successful());
        assertEquals("client_business_name_already_exists", result.rows().get(0).error());
        assertEquals("CREATED", result.rows().get(1).status());
    }

    @Test
    void dryRunDetectsDuplicateServiceNamesInSameFile() throws Exception {
        var rows = example("services.csv");
        var result = importer.execute("services", List.of(rows.get(0), new ImportRow(3, rows.get(0).values())), true);
        assertEquals(1, result.successful());
        assertEquals("import_duplicate_service_in_file", result.rows().get(1).error());
    }

    @Test
    void dispatchesUserAndPaymentFilesAndPassesDryRunToPaymentCreation() throws Exception {
        when(userPaymentHandler.user(any(), any())).thenReturn("user-uuid");
        when(userPaymentHandler.payment(any(), eq(true))).thenReturn("payment-uuid");
        assertEquals(12, importer.execute("users", example("users.csv"), true).successful());
        var payments = importer.execute("payments", example("payments.csv"), true);
        assertEquals(20, payments.successful());
        assertTrue(payments.rows().stream().allMatch(row -> row.hashId() == null));
        verify(userPaymentHandler, times(20)).payment(any(), eq(true));
    }

    @Test
    void rejectsTheLegacyRoleColumnWhenImportingUsers() {
        var legacyRole = new ImportRow(2, Map.of("fullName", "Legacy user", "email", "legacy@example.com", "role", "COLLECTOR"));
        var error = assertThrows(com.navi.net_pay_backend.domain.exception.DomainException.class,
                () -> importer.execute("users", List.of(legacyRole), true));
        assertEquals("import_unknown_columns", error.getMessage());
    }

    @Test
    void passesOptionalRoleHashIdFromImportRequestToEachUserRow() {
        when(userPaymentHandler.user(any(), any())).thenReturn("user-uuid");
        var row = new ImportRow(2, Map.of("fullName", "Selected role", "email", "selected@example.com"));
        importer.execute("users", List.of(row), true, "custom-role-uuid");
        verify(userPaymentHandler).user(row.values(), "custom-role-uuid");
    }

    @Test
    void duplicatesAreDetectedInUserAndPaymentPreviewsEvenAfterRollback() throws Exception {
        when(userPaymentHandler.user(any(), any())).thenReturn("user-uuid");
        when(userPaymentHandler.payment(any(), eq(true))).thenReturn("payment-uuid");
        var user = example("users.csv").get(0);
        var uppercase = new LinkedHashMap<>(user.values());
        uppercase.put("email", uppercase.get("email").toUpperCase(Locale.ROOT));
        var users = importer.execute("users", List.of(user, new ImportRow(3, uppercase)), true);
        assertEquals(1, users.failed());
        assertEquals("import_duplicate_user_in_file", users.rows().get(1).error());
        var payment = example("payments.csv").get(0);
        var payments = importer.execute("payments", List.of(payment, new ImportRow(3, payment.values())), true);
        assertEquals(1, payments.failed());
        assertEquals("import_duplicate_payment_in_file", payments.rows().get(1).error());
    }

    private List<ImportRow> example(String filename) throws Exception {
        try (var input = com.navi.net_pay_backend.support.ImportFixtures.input(filename)) {
            return new SpreadsheetImportReader().read(input, filename, ',');
        }
    }
}
