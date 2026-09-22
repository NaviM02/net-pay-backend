package com.navi.net_pay_backend.domain.service;

public interface PasswordResetTokenService {

    String generateToken();

    String hashToken(String token);

    boolean tokenMatches(String token, String tokenHash);

    int expiryMinutes();
}
