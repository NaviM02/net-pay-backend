package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.entity.TokenCredential;
import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenCredentialRepository {

    Long save(TokenCredential credential);

    void deleteByUserIdAndFingerprint(Long userId, String fingerprint);

    boolean existsByTokenAndExpirationDate(String token, LocalDateTime expirationDate);

    Optional<TokenCredential> findLastTokenByUserId(Long userId);
}