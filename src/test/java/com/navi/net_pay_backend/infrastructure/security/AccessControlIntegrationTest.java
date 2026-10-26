package com.navi.net_pay_backend.infrastructure.security;

import com.navi.net_pay_backend.application.model.RoleRequest;
import com.navi.net_pay_backend.application.model.UserRoleRequest;
import com.navi.net_pay_backend.application.use_case.AccessControlUseCase;
import com.navi.net_pay_backend.application.use_case.TokenCredentialUseCase;
import com.navi.net_pay_backend.domain.entity.Role;
import com.navi.net_pay_backend.domain.entity.UserRole;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AccessControlRepository;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.jpa.show-sql=false")
@Transactional
class AccessControlIntegrationTest {
    @Autowired private WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") private Filter securityFilter;
    @Autowired private AccessControlUseCase access;
    @Autowired private AccessControlRepository repository;
    @Autowired private AppUserRepository users;
    @Autowired private TokenCredentialUseCase tokens;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private jakarta.persistence.EntityManager entityManager;
    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.navi.net_pay_backend.domain.service.ReceiptStorageService receiptStorage;
    private MockMvc mvc;
    private long userId;
    private String userHashId;
    private String token;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilter).build();
        userHashId = UUID.randomUUID().toString();
        userId = jdbc.queryForObject("""
                INSERT INTO app_user(hash_id, full_name, email, password_hash, tp_status)
                SELECT ?, 'RBAC test', ?, 'unused', typology_id FROM adm_typology WHERE internal_id = 49 RETURNING id
                """, Long.class, userHashId, UUID.randomUUID() + "@example.com");
        token = tokens.create(users.findByIdAndStatusNot(userId, 51L).orElseThrow(), "rbac-test", "JUnit");
    }

    @Test
    void requiresAuthenticationAndDeniesUsersWithoutPermissionsButAllowsTheirOwnAccess() throws Exception {
        mvc.perform(get("/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
        mvc.perform(get("/api/roles")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/roles").header("Authorization", bearer())).andExpect(status().isForbidden());
        mvc.perform(get("/api/auth/access").header("Authorization", bearer()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.permissions").isEmpty());
        mvc.perform(get("/api/users/me").header("Authorization", bearer()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.hashId").value(userHashId))
                .andExpect(jsonPath("$.id").doesNotExist()).andExpect(jsonPath("$.tpRole").doesNotExist());
    }

    @Test
    void mergesRolesAndAppliesExpiryRevocationAndPermissionEditsToExistingToken() throws Exception {
        Role first = role("roles:read", "clients:read");
        Role second = role("roles:read", "payments:create");
        UserRole a = access.assign(userHashId, new UserRoleRequest(first.hashId(), Instant.now().minusSeconds(60), null));
        UserRole b = access.assign(userHashId, new UserRoleRequest(second.hashId(), Instant.now().minusSeconds(60), null));
        assertEquals(Set.of("roles:read", "clients:read", "payments:create"), access.effectivePermissions(userId));
        mvc.perform(get("/api/roles").header("Authorization", bearer())).andExpect(status().isOk());

        access.cancel(userHashId, a.hashId());
        assertEquals(Set.of("roles:read", "payments:create"), access.effectivePermissions(userId));
        // The same token must lose authorization as soon as the remaining grant expires.
        jdbc.update("UPDATE app_user_role SET ends_at = ? WHERE hash_id = ?", Timestamp.from(Instant.now().minusSeconds(1)), b.hashId());
        mvc.perform(get("/api/roles").header("Authorization", bearer())).andExpect(status().isForbidden());
        assertTrue(access.effectivePermissions(userId).isEmpty());

        jdbc.update("UPDATE app_user_role SET ends_at = NULL WHERE hash_id = ?", b.hashId());
        access.saveRole(second.hashId(), new RoleRequest(second.name(), null, "ACTIVE", List.of()));
        mvc.perform(get("/api/roles").header("Authorization", bearer())).andExpect(status().isForbidden());
    }

    @Test
    void dateBoundariesAndCancellationAreEnforcedAndHistoryIsKept() {
        Instant now = Instant.parse("2026-10-24T12:00:00Z");
        Role role = role("roles:read");
        UserRole grant = repository.assign(userId, role.hashId(), now, now.plusSeconds(60));
        assertTrue(repository.effectivePermissions(userId, now.minusMillis(1)).isEmpty());
        assertEquals(Set.of("roles:read"), repository.effectivePermissions(userId, now));
        assertTrue(repository.effectivePermissions(userId, now.plusSeconds(60)).isEmpty());
        assertThrows(DomainException.class, () -> access.assign(userHashId, new UserRoleRequest(role.hashId(), now, now)));
        assertThrows(EntityNotFoundException.class, () -> repository.cancel(-1L, grant.hashId(), now));
        repository.cancel(userId, grant.hashId(), now);
        assertTrue(repository.effectivePermissions(userId, now).isEmpty());
        assertEquals(1, access.assignments(userHashId).size());
        assertNotNull(access.assignments(userHashId).get(0).cancelledAt());
    }

    @Test
    void inactiveAndDeletedRolesAndInactiveUsersCannotAuthorize() throws Exception {
        Role role = role("roles:read");
        access.assign(userHashId, new UserRoleRequest(role.hashId(), null, null));
        access.saveRole(role.hashId(), new RoleRequest(role.name(), null, "INACTIVE", permissionIds("roles:read")));
        assertTrue(access.effectivePermissions(userId).isEmpty());
        assertThrows(DomainException.class, () -> access.assign(userHashId, new UserRoleRequest(role.hashId(), null, null)));
        access.saveRole(role.hashId(), new RoleRequest(role.name(), null, "ACTIVE", permissionIds("roles:read")));
        jdbc.update("UPDATE app_user SET tp_status = (SELECT typology_id FROM adm_typology WHERE internal_id = 50) WHERE id = ?", userId);
        entityManager.clear();
        mvc.perform(get("/api/auth/access").header("Authorization", bearer())).andExpect(status().isUnauthorized());
        jdbc.update("UPDATE app_user SET tp_status = (SELECT typology_id FROM adm_typology WHERE internal_id = 49) WHERE id = ?", userId);
        entityManager.clear();
        access.deleteRole(role.hashId());
        assertTrue(access.effectivePermissions(userId).isEmpty());
        assertThrows(EntityNotFoundException.class, () -> access.role(role.hashId()));
    }

    @Test
    void readDoesNotGrantWriteAndUserEditorsCannotAssignRoles() throws Exception {
        Role role = role("roles:read", "users:update");
        access.assign(userHashId, new UserRoleRequest(role.hashId(), null, null));
        mvc.perform(post("/api/roles").header("Authorization", bearer()).contentType("application/json")
                .content("{\"name\":\"Escalation\",\"permissionHashIds\":[]}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/users/" + userHashId + "/roles").header("Authorization", bearer())
                .contentType("application/json").content("{\"roleHashId\":\"" + role.hashId() + "\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void managesRolesAndDatedAssignmentsThroughHttp() throws Exception {
        Role manager = role("roles:read", "roles:create", "roles:update", "roles:delete", "permissions:read");
        access.assign(userHashId, new UserRoleRequest(manager.hashId(), null, null));
        String name = "HTTP " + UUID.randomUUID();
        String body = "{\"name\":\"" + name + "\",\"permissionHashIds\":[\"" + permissionIds("clients:read").get(0) + "\"]}";
        mvc.perform(get("/api/permissions").header("Authorization", bearer()))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].hashId").isString());
        mvc.perform(post("/api/roles").header("Authorization", bearer()).contentType("application/json").content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.permissions[0].authority").value("clients:read"));
        mvc.perform(get("/api/roles").param("search", name).param("size", "1").header("Authorization", bearer()))
                .andExpect(status().isOk()).andExpect(header().string("X-Total-Count", "1"))
                .andExpect(jsonPath("$[0].name").value(name));
        String hashId = jdbc.queryForObject("SELECT hash_id FROM app_role WHERE name = ?", String.class, name);
        mvc.perform(put("/api/roles/" + hashId).header("Authorization", bearer()).contentType("application/json")
                .content("{\"name\":\"" + name + "\",\"status\":\"INACTIVE\",\"permissionHashIds\":[]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("INACTIVE"));
        mvc.perform(put("/api/roles/" + hashId).header("Authorization", bearer()).contentType("application/json").content(body))
                .andExpect(status().isOk());
        String path = "/api/users/" + userHashId + "/roles";
        mvc.perform(post(path).header("Authorization", bearer()).contentType("application/json")
                .content("{\"roleHashId\":\"" + hashId + "\",\"startsAt\":\"2099-01-01T00:00:00Z\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.endsAt").isEmpty())
                .andExpect(jsonPath("$.effective").value(false));
        String assignment = jdbc.queryForObject("""
                SELECT ur.hash_id FROM app_user_role ur JOIN app_role r ON r.id = ur.role_id
                WHERE ur.user_id = ? AND r.hash_id = ?
                """, String.class, userId, hashId);
        mvc.perform(delete(path + "/" + assignment).header("Authorization", bearer())).andExpect(status().isNoContent());
        mvc.perform(post(path).header("Authorization", bearer()).contentType("application/json")
                .content("{\"roleHashId\":\"" + hashId + "\",\"startsAt\":\"not-a-date\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/api/roles/" + hashId).header("Authorization", bearer())).andExpect(status().isNoContent());
        mvc.perform(get("/api/roles/" + hashId).header("Authorization", bearer())).andExpect(status().isNotFound());
    }

    @Test
    void duplicateRoleNamesReturnConflictIgnoringCase() throws Exception {
        Role manager = role("roles:create");
        access.assign(userHashId, new UserRoleRequest(manager.hashId(), null, null));
        mvc.perform(post("/api/roles").header("Authorization", bearer()).contentType("application/json")
                .content("{\"name\":\"" + manager.name().toUpperCase(Locale.ROOT) + "\",\"permissionHashIds\":[]}"))
                .andExpect(status().isConflict());
    }

    @Test
    void lockedRolesAreAssignableAndProtectedByTheirStatus() {
        var lockedRoles = access.roles().stream().filter(r -> "LOCKED".equals(r.status())).toList();
        assertEquals(2, lockedRoles.size());
        for (Role locked : lockedRoles) {
            assertEquals(52L, locked.statusInternalId());
            UserRole assignment = access.assign(userHashId, new UserRoleRequest(locked.hashId(), null, null));
            assertTrue(assignment.isEffectiveAt(Instant.now()));
            assertTrue(access.effectivePermissions(userId).containsAll(locked.permissions().stream().map(p -> p.authority()).toList()));
            assertEquals("role_locked", assertThrows(DomainException.class, () -> access.saveRole(locked.hashId(),
                    new RoleRequest("Changed", null, "ACTIVE", List.of()))).getMessage());
            assertEquals("role_locked", assertThrows(DomainException.class, () -> access.deleteRole(locked.hashId())).getMessage());
        }
    }

    @Test
    void personalPagePermissionsAreAvailableToCustomRolesAndUseTheExistingEndpoints() throws Exception {
        Set<String> expected = Set.of("my-dashboard:read", "my-clients:read", "my-payments:read",
                "my-payments:create", "my-receipts:read");
        Set<String> catalogue = new HashSet<>(expected);
        catalogue.add("my-receipts:export");
        assertEquals(catalogue, new HashSet<>(access.permissions().stream()
                .filter(p -> p.resource().startsWith("my-")).map(p -> p.authority()).toList()));
        assertTrue(access.permissions().stream().noneMatch(p -> "typologies".equals(p.resource())));
        Role pages = role(expected.toArray(String[]::new));
        access.assign(userHashId, new UserRoleRequest(pages.hashId(), null, null));
        assertEquals(expected, access.effectivePermissions(userId));
        mvc.perform(get("/api/auth/access").header("Authorization", bearer()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.permissions.length()").value(5));
        for (String path : List.of("/api/dashboard/collector", "/api/clients", "/api/payments", "/api/receipts",
                "/api/users/" + userHashId + "/clients")) {
            mvc.perform(get(path).param("collectorId", String.valueOf(userId)).header("Authorization", bearer()))
                    .andExpect(status().isOk());
        }
        mvc.perform(get("/api/dashboard").header("Authorization", bearer())).andExpect(status().isForbidden());
        mvc.perform(get("/api/roles").header("Authorization", bearer())).andExpect(status().isForbidden());
        long service = jdbc.queryForObject("""
                INSERT INTO app_service(hash_id, name, speed, monthly_price, tp_type, tp_status)
                VALUES (?, 'Personal pages test', '10 Mbps', 100, 30071, 30049) RETURNING id
                """, Long.class, UUID.randomUUID().toString());
        String clientHashId = UUID.randomUUID().toString();
        long client = jdbc.queryForObject("""
                INSERT INTO client(hash_id, business_name, contact_name, service_id, collector_id, tp_status)
                VALUES (?, 'Personal pages client', 'Test contact', ?, ?, 30049) RETURNING id
                """, Long.class, clientHashId, service, userId);
        mvc.perform(get("/api/clients/" + clientHashId).header("Authorization", bearer())).andExpect(status().isOk());
        mvc.perform(post("/api/payments/collector").header("Authorization", bearer()).contentType("application/json")
                        .content("{\"client\":{\"id\":" + client + "},\"collector\":{\"id\":-1},\"amount\":10,\"paidAt\":\""
                                + java.time.LocalDate.now() + "\",\"tpPaymentMethod\":{\"internalId\":81},\"tpPaymentStatus\":{\"internalId\":92}}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.collector.hashId").value(userHashId));
        String paymentHashId = jdbc.queryForObject("SELECT hash_id FROM payment WHERE client_id = ?", String.class, client);
        mvc.perform(get("/api/payments/" + paymentHashId).header("Authorization", bearer())).andExpect(status().isOk());
    }

    @Test
    void personalReceiptExportRendersThePdfAndRecordsPrintingThroughExistingEndpoints() throws Exception {
        Role printing = role("my-receipts:read", "my-receipts:export");
        access.assign(userHashId, new UserRoleRequest(printing.hashId(), null, null));
        long service = jdbc.queryForObject("""
                INSERT INTO app_service(hash_id, name, speed, monthly_price, tp_type, tp_status)
                VALUES (?, 'Printing test', '10 Mbps', 100, 30071, 30049) RETURNING id
                """, Long.class, UUID.randomUUID().toString());
        long client = jdbc.queryForObject("""
                INSERT INTO client(hash_id, business_name, contact_name, service_id, collector_id, tp_status)
                VALUES (?, 'Printing client', 'Test contact', ?, ?, 30049) RETURNING id
                """, Long.class, UUID.randomUUID().toString(), service, userId);
        String paymentHashId = UUID.randomUUID().toString();
        long payment = jdbc.queryForObject("""
                INSERT INTO payment(hash_id, client_id, service_id, collector_id, amount, paid_at, tp_payment_method, tp_payment_status)
                VALUES (?, ?, ?, ?, 25, CURRENT_DATE, 30081, 30091) RETURNING id
                """, Long.class, paymentHashId, client, service, userId);
        String receiptHashId = UUID.randomUUID().toString();
        jdbc.update("""
                INSERT INTO receipt(hash_id, receipt_number, payment_id, client_id, amount, collector_id)
                VALUES (?, ?, ?, ?, 25, ?)
                """, receiptHashId, "TEST-" + UUID.randomUUID().toString().substring(0, 20), payment, client, userId);
        mvc.perform(get("/api/receipts/" + receiptHashId + "/document").header("Authorization", bearer()))
                .andExpect(status().isOk()).andExpect(content().contentType("application/pdf"));
        mvc.perform(get("/api/payments/" + paymentHashId + "/receipt/pdf").header("Authorization", bearer()))
                .andExpect(status().isOk()).andExpect(content().contentType("application/pdf"));
        mvc.perform(post("/api/receipts/" + receiptHashId + "/print").header("Authorization", bearer()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.printedAt").isNotEmpty());
        entityManager.flush();
        assertNotNull(jdbc.queryForObject("SELECT printed_at FROM receipt WHERE hash_id = ?", java.sql.Timestamp.class, receiptHashId));
    }

    @Test
    void typologyLookupsDoNotNeedAnAssignablePermissionAndRemainReadOnly() throws Exception {
        mvc.perform(get("/api/typologies/49").header("Authorization", bearer())).andExpect(status().isOk());
        mvc.perform(get("/api/typologies/by-parent/48").header("Authorization", bearer())).andExpect(status().isOk());
        mvc.perform(get("/api/typologies/49")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/typologies").header("Authorization", bearer()).contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void defaultCollectorSeedUsesPersonalPagePermissions() {
        var permissions = access.role("bb66bb4f-020e-44de-b604-8a3153d74862").permissions().stream()
                .map(p -> p.authority()).toList();
        assertTrue(permissions.containsAll(List.of("my-dashboard:read", "my-clients:read", "my-payments:read",
                "my-payments:create", "my-receipts:read", "my-receipts:export")));
        assertTrue(Collections.disjoint(permissions, List.of("typologies:read", "dashboard:read", "clients:read",
                "payments:read", "payments:create", "receipts:read")));
    }

    @Test
    void migratedAdministratorsKeepAllCataloguePermissionsAndTypologyColumnIsGone() {
        Set<String> cataloguePermissions = new HashSet<>(access.permissions().stream().map(p -> p.authority()).toList());
        Set<String> administratorPermissions = access.role("bb66bb4f-020e-44de-b604-8a3153d74861")
                .permissions().stream().map(p -> p.authority()).collect(java.util.stream.Collectors.toSet());
        assertEquals(cataloguePermissions, administratorPermissions);
        assertEquals(0, jdbc.queryForObject("""
                SELECT count(*) FROM information_schema.columns WHERE table_name = 'app_user' AND column_name = 'tp_role'
                """, Integer.class));
    }

    private String bearer() { return "Bearer " + token; }
    private List<String> permissionIds(String... authorities) {
        Set<String> wanted = Set.of(authorities);
        return access.permissions().stream().filter(p -> wanted.contains(p.authority())).map(p -> p.hashId()).toList();
    }
    private Role role(String... permissions) {
        return access.saveRole(null, new RoleRequest("Test " + UUID.randomUUID(), null, "ACTIVE", permissionIds(permissions)));
    }
}
