package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.service.TokenPayload;
import com.navi.net_pay_backend.domain.service.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtTokenService implements TokenService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtTokenService(@Value("${security.jwt.secret}") String secret, @Value("${security.jwt.expiration}") long expiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    @Override
    public String generateToken(String username, Long userId, Long roleId) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + (expiration * 1000));

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roleId", roleId)
                .issuer("NETPAY")
                .audience().add("NETPAY").and()
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public TokenPayload verifyToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = claims.get("userId", Long.class);
            Long roleId = claims.get("roleId", Long.class);

            return new TokenPayload(userId, roleId);

        } catch (Exception e) {
            return null;
        }
    }
}