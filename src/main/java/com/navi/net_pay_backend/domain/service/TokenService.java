package com.navi.net_pay_backend.domain.service;

public interface TokenService {
    String generateToken(String username, Long userId, Long roleId);
    TokenPayload verifyToken(String token);
}