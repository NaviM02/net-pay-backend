package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.entity.TokenCredential;
import com.navi.net_pay_backend.domain.repository.TokenCredentialRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.TokenCredentialEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DbTokenCredentialRepository implements TokenCredentialRepository {

    private final TokenCredentialJpaRepository repository;

    @Override
    @Transactional
    public Long save(TokenCredential credential) {

        TokenCredentialEntity entity = TokenCredentialEntity.builder()
                .userId(credential.getUserId())
                .token(credential.getToken())
                .effectiveDate(credential.getEffectiveDate())
                .expirationDate(credential.getExpirationDate())
                .userAgent(credential.getUserAgent())
                .fingerprint(credential.getFingerprint())
                .createdAt(LocalDateTime.now())
                .build();

        return repository.save(entity).getId();
    }

    @Override
    @Transactional
    public void deleteByUserIdAndFingerprint(Long userId, String fingerprint) {
        repository.deleteByUserIdAndFingerprint(userId, fingerprint);
    }

    @Override
    public boolean existsByTokenAndExpirationDate(String token, LocalDateTime expirationDate) {
        return repository.existsByTokenAndExpirationDateAfter(token, expirationDate);
    }

    @Override
    public Optional<TokenCredential> findLastTokenByUserId(Long userId) {
        return repository.findFirstByUserIdOrderByIdDesc(userId).map(this::toDomain);
    }

    private TokenCredential toDomain(TokenCredentialEntity entity) {
        return TokenCredential.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .token(entity.getToken())
                .effectiveDate(entity.getEffectiveDate())
                .expirationDate(entity.getExpirationDate())
                .userAgent(entity.getUserAgent())
                .fingerprint(entity.getFingerprint())
                .build();
    }
}