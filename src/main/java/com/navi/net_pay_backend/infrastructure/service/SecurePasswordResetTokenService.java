package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.service.PasswordResetTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class SecurePasswordResetTokenService implements PasswordResetTokenService {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom random = new SecureRandom();
    private final int expiryMinutes;

    public SecurePasswordResetTokenService(
            @Value("${security.password-reset.expiration-minutes:30}") int expiryMinutes) {
        this.expiryMinutes = expiryMinutes;
    }

    @Override
    public String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Override
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new DomainException("reset_token_hash_failed");
        }
    }

    @Override
    public boolean tokenMatches(String token, String tokenHash) {
        return token != null && tokenHash != null && tokenHash.equals(hashToken(token));
    }

    @Override
    public int expiryMinutes() {
        return expiryMinutes;
    }
}
