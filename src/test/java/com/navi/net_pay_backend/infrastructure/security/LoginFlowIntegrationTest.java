package com.navi.net_pay_backend.infrastructure.security;

import com.jayway.jsonpath.JsonPath;
import com.navi.net_pay_backend.domain.service.PasswordHashService;
import com.navi.net_pay_backend.domain.service.EmailSender;
import com.navi.net_pay_backend.domain.dto.EmailMessage;
import com.navi.net_pay_backend.domain.exception.DomainException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.ArgumentCaptor;
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
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** No outer test transaction: JWT authentication executes before MVC opens a JPA session. */
@SpringBootTest(properties = "spring.jpa.show-sql=false")
class LoginFlowIntegrationTest {
    @Autowired private WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") private Filter securityFilter;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PasswordHashService passwords;
    @MockitoBean private EmailSender emailSender;
    private MockMvc mvc;
    private Long userId;
    private String hashId;
    private String email;
    private String password;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).addFilters(securityFilter).build();
        hashId = UUID.randomUUID().toString();
        email = hashId + "@example.com";
        password = UUID.randomUUID().toString();
        userId = jdbc.queryForObject("""
                INSERT INTO app_user(hash_id, full_name, email, password_hash, tp_status)
                SELECT ?, 'Login regression', ?, ?, typology_id FROM adm_typology WHERE internal_id = 49 RETURNING id
                """, Long.class, hashId, email, passwords.hash(password));
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
        if (userId != null) {
            jdbc.update("DELETE FROM audit_log WHERE user_id = ? OR entity_hash_id = ?", userId, hashId);
            jdbc.update("DELETE FROM login_challenge WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM app_user_role WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM token_credential WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM app_user WHERE id = ?", userId);
        }
    }

    @Test
    void loginIsPublicAndItsTokenAuthenticatesTheFollowingRequestsWithoutAnOuterTransaction() throws Exception {
        String token = login(null);
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.hashId").value(hashId));
        mvc.perform(get("/api/auth/access").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.permissions").isEmpty());
    }

    @Test
    void administratorLoginWorksWithAnExpiredOrInvalidPreviousToken() throws Exception {
        jdbc.update("""
                INSERT INTO app_user_role(hash_id, user_id, role_id)
                SELECT ?, ?, id FROM app_role WHERE hash_id = 'bb66bb4f-020e-44de-b604-8a3153d74861'
                """, UUID.randomUUID().toString(), userId);
        String token = login("Bearer expired-or-invalid-token");
        mvc.perform(get("/api/roles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void badCredentialsAndMalformedBodiesKeepTheirOwnPublicEndpointErrors() throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"incorrect\"}"))
                .andExpect(status().isUnauthorized()).andExpect(content().string("wrong_password"));
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{"))
                .andExpect(status().isBadRequest()).andExpect(content().string("invalid_request_body"));
        mvc.perform(get("/api/users/reset-password/validate").param("token", "invalid"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void twoFactorLoginDoesNotIssueTokenUntilCodeIsVerifiedAndCodeCannotBeReplayed() throws Exception {
        String[] challenge = beginTwoFactor();
        assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM token_credential WHERE user_id = ?", Long.class, userId));
        assertEquals(null, jdbc.queryForObject("SELECT last_login_at FROM app_user WHERE id = ?", Object.class, userId));
        String storedHash = jdbc.queryForObject("SELECT code_hash FROM login_challenge WHERE hash_id = ?", String.class, challenge[0]);
        assertTrue(passwords.compare(challenge[1], storedHash));
        var response = mvc.perform(post("/api/auth/2fa/verify").contentType("application/json")
                        .content(verificationBody(challenge[0], challenge[1])))
                .andExpect(status().isOk()).andExpect(jsonPath("$.twoFactorRequired").value(false))
                .andExpect(jsonPath("$.authc").isString()).andReturn().getResponse();
        String token = JsonPath.read(response.getContentAsString(), "$.authc");
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.twoFactorEnabled").value(true));
        mvc.perform(post("/api/auth/2fa/verify").contentType("application/json")
                        .content(verificationBody(challenge[0], challenge[1])))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void failedAttemptsPersistAndLockChallengeEvenWhenCorrectCodeIsLaterSupplied() throws Exception {
        String[] challenge = beginTwoFactor();
        for (int attempt = 0; attempt < 5; attempt++) {
            mvc.perform(post("/api/auth/2fa/verify").contentType("application/json")
                            .content(verificationBody(challenge[0], "wrong")))
                    .andExpect(status().isUnauthorized());
        }
        assertEquals(5, jdbc.queryForObject("SELECT attempts FROM login_challenge WHERE hash_id = ?", Integer.class, challenge[0]));
        mvc.perform(post("/api/auth/2fa/verify").contentType("application/json")
                        .content(verificationBody(challenge[0], challenge[1])))
                .andExpect(status().isUnauthorized());
        assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM token_credential WHERE user_id = ?", Long.class, userId));
    }

    @Test
    void expiredChallengeAndChangedUserEmailAreRejected() throws Exception {
        String[] challenge = beginTwoFactor();
        jdbc.update("UPDATE app_user SET email = ? WHERE id = ?", "changed-" + email, userId);
        mvc.perform(post("/api/auth/2fa/verify").contentType("application/json")
                        .content(verificationBody(challenge[0], challenge[1])))
                .andExpect(status().isUnauthorized());
        jdbc.update("UPDATE app_user SET email = ? WHERE id = ?", email, userId);
        jdbc.update("UPDATE login_challenge SET expires_at = CURRENT_TIMESTAMP - INTERVAL '1 minute' WHERE hash_id = ?", challenge[0]);
        mvc.perform(post("/api/auth/2fa/verify").contentType("application/json")
                        .content(verificationBody(challenge[0], challenge[1])))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void twoFactorEmailFailuresRollBackChallengeAndNeverIssueToken() throws Exception {
        jdbc.update("UPDATE app_user SET two_factor_enabled = true WHERE id = ?", userId);
        doThrow(new DomainException("email_send_failed")).when(emailSender).send(any());
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isBadRequest());
        assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM login_challenge WHERE user_id = ?", Long.class, userId));
        assertEquals(0L, jdbc.queryForObject("SELECT count(*) FROM token_credential WHERE user_id = ?", Long.class, userId));
    }

    @Test
    void userCanEnableTwoFactorFromProfileAndRepeatedLoginIsThrottled() throws Exception {
        String token = login(null);
        mvc.perform(put("/api/users/me").header("Authorization", "Bearer " + token)
                        .contentType("application/json").content("{\"twoFactorEnabled\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.twoFactorEnabled").value(true));
        beginTwoFactor();
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isBadRequest()).andExpect(content().string("two_factor_retry_later"));
        verify(emailSender, times(1)).send(any());
    }

    @Test
    void administratorCanCreateAndEditTwoFactorFlagAndOmittedFlagPreservesItsValue() throws Exception {
        jdbc.update("""
                INSERT INTO app_user_role(hash_id, user_id, role_id)
                SELECT ?, ?, id FROM app_role WHERE hash_id = 'bb66bb4f-020e-44de-b604-8a3153d74861'
                """, UUID.randomUUID().toString(), userId);
        String token = login(null);
        String createdHashId = null;
        try {
            var response = mvc.perform(post("/api/users").header("Authorization", "Bearer " + token)
                            .contentType("application/json").content("{\"fullName\":\"Two factor user\",\"email\":\"new-" + email
                                    + "\",\"password\":\"temporary-user-password\",\"twoFactorEnabled\":true}"))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.twoFactorEnabled").value(true))
                    .andReturn().getResponse();
            createdHashId = JsonPath.read(response.getContentAsString(), "$.hashId");
            mvc.perform(put("/api/users/" + createdHashId).header("Authorization", "Bearer " + token)
                            .contentType("application/json").content("{\"fullName\":\"Updated name\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.twoFactorEnabled").value(true));
            mvc.perform(put("/api/users/" + createdHashId).header("Authorization", "Bearer " + token)
                            .contentType("application/json").content("{\"fullName\":\"Updated name\",\"twoFactorEnabled\":false}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.twoFactorEnabled").value(false));
        } finally {
            if (createdHashId != null) {
                jdbc.update("DELETE FROM audit_log WHERE entity_hash_id = ?", createdHashId);
                jdbc.update("DELETE FROM app_user WHERE hash_id = ?", createdHashId);
            }
        }
    }

    @Test
    void reportExportsDefaultToNativeXlsAndRejectCsv() throws Exception {
        jdbc.update("""
                INSERT INTO app_user_role(hash_id, user_id, role_id)
                SELECT ?, ?, id FROM app_role WHERE hash_id = 'bb66bb4f-020e-44de-b604-8a3153d74861'
                """, UUID.randomUUID().toString(), userId);
        String token = login(null);
        for (String report : new String[]{"collections", "overdue-clients"}) {
            var response = mvc.perform(get("/api/reports/" + report + "/export")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andExpect(content().contentType("application/vnd.ms-excel"))
                    .andReturn().getResponse();
            assertTrue(response.getHeader("Content-Disposition").endsWith(".xls\""));
            try (var workbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook(
                    new java.io.ByteArrayInputStream(response.getContentAsByteArray()))) {
                assertEquals(1, workbook.getNumberOfSheets());
            }
            mvc.perform(get("/api/reports/" + report + "/export").param("format", "csv")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isBadRequest()).andExpect(content().string("unsupported_report_format"));
        }
    }

    private String[] beginTwoFactor() throws Exception {
        jdbc.update("UPDATE app_user SET two_factor_enabled = true WHERE id = ?", userId);
        var response = mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"fingerprint\":\"2fa-test\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.twoFactorRequired").value(true))
                .andExpect(jsonPath("$.authc").doesNotExist()).andExpect(header().doesNotExist("Authorization"))
                .andReturn().getResponse();
        var captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(captor.capture());
        assertEquals(email, captor.getValue().getTo());
        var matcher = java.util.regex.Pattern.compile("<h2>([0-9]{6})</h2>").matcher(captor.getValue().getHtmlBody());
        assertTrue(matcher.find());
        return new String[]{JsonPath.read(response.getContentAsString(), "$.challengeId"), matcher.group(1)};
    }

    private String verificationBody(String challengeId, String code) {
        return "{\"challengeId\":\"" + challengeId + "\",\"code\":\"" + code + "\"}";
    }

    private String login(String previousToken) throws Exception {
        var request = post("/api/auth/login").contentType("application/json")
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"fingerprint\":\"login-regression\"}");
        if (previousToken != null) request.header("Authorization", previousToken);
        var response = mvc.perform(request).andExpect(status().isOk())
                .andExpect(jsonPath("$.authc").isString()).andReturn().getResponse();
        String token = JsonPath.read(response.getContentAsString(), "$.authc");
        assertEquals("Bearer " + token, response.getHeader("Authorization"));
        return token;
    }
}
