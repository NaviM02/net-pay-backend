package com.navi.net_pay_backend.infrastructure.security;

import com.jayway.jsonpath.JsonPath;
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
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** No outer test transaction: JWT authentication executes before MVC opens a JPA session. */
@SpringBootTest(properties = "spring.jpa.show-sql=false")
class LoginFlowIntegrationTest {
    @Autowired private WebApplicationContext context;
    @Autowired @Qualifier("springSecurityFilterChain") private Filter securityFilter;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PasswordHashService passwords;
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
