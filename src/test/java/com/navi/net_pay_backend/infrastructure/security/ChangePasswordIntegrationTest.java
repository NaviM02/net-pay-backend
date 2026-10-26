package com.navi.net_pay_backend.infrastructure.security;

import com.navi.net_pay_backend.application.use_case.TokenCredentialUseCase;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.service.PasswordHashService;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.AfterEach;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.jpa.show-sql=false")
@Transactional
class ChangePasswordIntegrationTest {
    private static final String CURRENT_PASSWORD = "actor-password";
    private static final String TARGET_PASSWORD = "target-password";
    private static final String NEW_PASSWORD = "new-password";

    @Autowired private WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") private Filter securityFilter;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PasswordHashService passwords;
    @Autowired private AppUserRepository users;
    @Autowired private TokenCredentialUseCase tokens;
    private MockMvc mvc;
    private String actorHashId;
    private String targetHashId;
    private long actorId;
    private String token;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilter).build();
        actorHashId = UUID.randomUUID().toString();
        targetHashId = UUID.randomUUID().toString();
        actorId = createUser(actorHashId, CURRENT_PASSWORD);
        createUser(targetHashId, TARGET_PASSWORD);
        token = tokens.create(users.findByIdAndStatusNot(actorId, 51L).orElseThrow(), "password-test", "JUnit");
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void profileChangesOnlyTheAuthenticatedUserWithoutAdministrativePermissions() throws Exception {
        mvc.perform(put("/api/users/me/password").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content(body(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isOk());
        assertPassword(actorHashId, NEW_PASSWORD);
        assertPassword(targetHashId, TARGET_PASSWORD);
        assertNull(users.findByPublicIdAndStatusNot(actorHashId, 51L).orElseThrow().getResetTokenHash());
        assertNull(users.findByPublicIdAndStatusNot(actorHashId, 51L).orElseThrow().getResetTokenExpiresAt());
        assertEquals(1, jdbc.queryForObject("""
                SELECT count(*) FROM audit_log
                WHERE action = 'user_password_changed' AND entity_hash_id = ? AND user_id = ?
                """, Integer.class, actorHashId, actorId));
    }

    @Test
    void crudRequiresPermissionAndChecksTheActorsPasswordRatherThanTheTargets() throws Exception {
        String path = "/api/users/" + targetHashId + "/password";
        mvc.perform(put(path).header("Authorization", "Bearer " + token).contentType("application/json")
                        .content(body(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isForbidden());
        grantUpdate();
        mvc.perform(put(path).header("Authorization", "Bearer " + token).contentType("application/json")
                        .content(body(TARGET_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isUnauthorized()).andExpect(content().string("wrong_password"));
        assertPassword(targetHashId, TARGET_PASSWORD);
        mvc.perform(put(path).header("Authorization", "Bearer " + token).contentType("application/json")
                        .content(body(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isOk());
        assertPassword(targetHashId, NEW_PASSWORD);
        assertPassword(actorHashId, CURRENT_PASSWORD);
    }

    @Test
    void rejectsInvalidPasswordsWithoutChangingEitherUser() throws Exception {
        String[][] cases = {
                {"", NEW_PASSWORD, NEW_PASSWORD, "current_password_required", "400"},
                {"wrong", NEW_PASSWORD, NEW_PASSWORD, "wrong_password", "401"},
                {CURRENT_PASSWORD, "", "", "password_required", "400"},
                {CURRENT_PASSWORD, "short", "short", "password_too_short", "400"},
                {CURRENT_PASSWORD, NEW_PASSWORD, "different", "password_confirmation_mismatch", "400"}
        };
        for (String[] values : cases) {
            mvc.perform(put("/api/users/me/password").header("Authorization", "Bearer " + token)
                            .contentType("application/json").content(body(values[0], values[1], values[2])))
                    .andExpect(status().is(Integer.parseInt(values[4])))
                    .andExpect(content().string(values[3]));
        }
        assertPassword(actorHashId, CURRENT_PASSWORD);
        assertPassword(targetHashId, TARGET_PASSWORD);
    }

    @Test
    void requiresAuthenticationForBothEndpoints() throws Exception {
        for (String path : new String[]{"/api/users/me/password", "/api/users/" + targetHashId + "/password"}) {
            mvc.perform(put(path).contentType("application/json")
                            .content(body(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void missingAndDeletedTargetsAreNotChanged() throws Exception {
        grantUpdate();
        jdbc.update("""
                UPDATE app_user SET tp_status = (SELECT typology_id FROM adm_typology WHERE internal_id = 51)
                WHERE hash_id = ?
                """, targetHashId);
        for (String hashId : new String[]{targetHashId, UUID.randomUUID().toString()}) {
            mvc.perform(put("/api/users/" + hashId + "/password").header("Authorization", "Bearer " + token)
                            .contentType("application/json").content(body(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)))
                    .andExpect(status().isNotFound()).andExpect(content().string("user_not_found"));
        }
    }

    @Test
    void ordinaryUserUpdateCannotBypassPasswordVerification() throws Exception {
        grantUpdate();
        mvc.perform(put("/api/users/" + targetHashId).header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("{\"fullName\":\"Test\",\"password\":\"new-password\"}"))
                .andExpect(status().isBadRequest()).andExpect(content().string("use_change_password_endpoint"));
        assertPassword(targetHashId, TARGET_PASSWORD);
    }

    private long createUser(String hashId, String password) {
        return jdbc.queryForObject("""
                INSERT INTO app_user(hash_id, full_name, email, password_hash, tp_status, reset_token_hash, reset_token_expires_at)
                SELECT ?, 'Password test', ?, ?, typology_id, ?, CURRENT_TIMESTAMP + INTERVAL '1 hour'
                FROM adm_typology WHERE internal_id = 49 RETURNING id
                """, Long.class, hashId, hashId + "@example.com", passwords.hash(password), UUID.randomUUID().toString());
    }

    private void grantUpdate() {
        String roleHashId = UUID.randomUUID().toString();
        Long roleId = jdbc.queryForObject("""
                INSERT INTO app_role(hash_id, name, tp_status)
                SELECT ?, ?, typology_id FROM adm_typology WHERE internal_id = 49 RETURNING id
                """, Long.class, roleHashId, roleHashId);
        jdbc.update("""
                INSERT INTO app_role_permission(role_id, permission_id)
                SELECT ?, id FROM app_permission WHERE resource = 'users' AND action = 'update'
                """, roleId);
        jdbc.update("INSERT INTO app_user_role(hash_id, user_id, role_id) VALUES (?, ?, ?)",
                UUID.randomUUID().toString(), actorId, roleId);
    }

    private void assertPassword(String hashId, String expected) {
        String hash = users.findByPublicIdAndStatusNot(hashId, 51L).orElseThrow().getPassword();
        assertNotEquals(expected, hash);
        assertTrue(passwords.compare(expected, hash));
    }

    private String body(String current, String password, String confirmation) {
        return "{\"currentPassword\":\"" + current + "\",\"newPassword\":\"" + password
                + "\",\"confirmPassword\":\"" + confirmation + "\"}";
    }
}
