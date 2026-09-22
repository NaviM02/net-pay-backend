package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.dto.EmailMessage;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.service.EmailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class GmailEmailSender implements EmailSender {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String SEND_URL = "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";
    private static final long EXPIRY_SKEW_SECONDS = 60;

    private final RestClient restClient = RestClient.create();
    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;
    private final String fromEmail;

    private volatile String accessToken;
    private volatile long accessTokenExpiresAt;

    public GmailEmailSender(
            @Value("${gmail.api.client-id:}") String clientId,
            @Value("${gmail.api.client-secret:}") String clientSecret,
            @Value("${gmail.api.refresh-token:}") String refreshToken,
            @Value("${gmail.api.from-email:}") String fromEmail) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.fromEmail = fromEmail;
    }

    @Override
    public void send(EmailMessage message) {
        requireConfigured();
        try {
            restClient.post()
                    .uri(SEND_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("raw", buildRawMessage(message)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (DomainException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.error("Gmail send failed: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new DomainException("email_send_failed");
        } catch (Exception ex) {
            log.error("Gmail send error", ex);
            throw new DomainException("email_send_failed");
        }
    }

    private String accessToken() {
        long now = Instant.now().getEpochSecond();
        if (isTokenFresh(now)) {
            return accessToken;
        }
        synchronized (this) {
            long current = Instant.now().getEpochSecond();
            if (isTokenFresh(current)) {
                return accessToken;
            }
            refreshAccessToken();
            return accessToken;
        }
    }

    private boolean isTokenFresh(long now) {
        return accessToken != null && now < accessTokenExpiresAt - EXPIRY_SKEW_SECONDS;
    }

    @SuppressWarnings("unchecked")
    private void refreshAccessToken() {
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);
            form.add("refresh_token", refreshToken);
            form.add("grant_type", "refresh_token");

            Map<String, Object> response = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            String token = response != null ? (String) response.get("access_token") : null;
            if (token == null || token.isBlank()) {
                throw new DomainException("email_auth_failed");
            }
            long expiresIn = response.get("expires_in") instanceof Number number
                    ? number.longValue()
                    : 3600L;

            this.accessToken = token;
            this.accessTokenExpiresAt = Instant.now().getEpochSecond() + expiresIn;
        } catch (DomainException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.error("Gmail OAuth token refresh failed: status={} body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new DomainException("email_auth_failed");
        } catch (Exception ex) {
            log.error("Gmail OAuth token refresh error", ex);
            throw new DomainException("email_auth_failed");
        }
    }

    private String buildRawMessage(EmailMessage message) {
        String html = message.getHtmlBody() != null ? message.getHtmlBody() : "";
        String mime = "From: " + fromEmail + "\r\n"
                + "To: " + message.getTo() + "\r\n"
                + "Subject: " + encodeHeader(message.getSubject()) + "\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Transfer-Encoding: base64\r\n"
                + "\r\n"
                + Base64.getMimeEncoder().encodeToString(html.getBytes(StandardCharsets.UTF_8));

        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(mime.getBytes(StandardCharsets.UTF_8));
    }

    private String encodeHeader(String value) {
        String safe = value != null ? value : "";
        boolean ascii = safe.chars().allMatch(c -> c < 128);
        if (ascii) {
            return safe;
        }
        return "=?UTF-8?B?" + Base64.getEncoder().encodeToString(safe.getBytes(StandardCharsets.UTF_8)) + "?=";
    }

    private void requireConfigured() {
        if (isBlank(clientId) || isBlank(clientSecret) || isBlank(refreshToken) || isBlank(fromEmail)) {
            throw new DomainException("email_not_configured");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
