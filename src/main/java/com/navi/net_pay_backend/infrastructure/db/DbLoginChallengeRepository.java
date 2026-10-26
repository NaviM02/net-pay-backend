package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.entity.LoginChallenge;
import com.navi.net_pay_backend.domain.repository.LoginChallengeRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.LoginChallengeEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppUserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class DbLoginChallengeRepository implements LoginChallengeRepository {
    private final LoginChallengeJpaRepository repository;
    private final EntityManager entityManager;

    @Override
    public void lockUser(Long userId) {
        entityManager.find(AppUserEntity.class, userId, LockModeType.PESSIMISTIC_WRITE);
    }

    @Override
    public Optional<LocalDateTime> latestCreatedAt(Long userId) {
        return repository.findFirstByUserIdOrderByCreatedAtDesc(userId).map(LoginChallengeEntity::getCreatedAt);
    }

    @Override
    public void save(LoginChallenge challenge) {
        LoginChallengeEntity entity = new LoginChallengeEntity();
        entity.setHashId(challenge.getHashId());
        entity.setUserId(challenge.getUserId());
        entity.setCodeHash(challenge.getCodeHash());
        entity.setExpiresAt(challenge.getExpiresAt());
        entity.setCreatedAt(challenge.getCreatedAt());
        entity.setAttempts(challenge.getAttempts());
        entity.setConsumed(challenge.isConsumed());
        entity.setFingerprint(challenge.getFingerprint());
        entity.setUserAgent(challenge.getUserAgent());
        entity.setEmail(challenge.getEmail());
        entity.setPasswordHash(challenge.getPasswordHash());
        repository.save(entity);
    }

    @Override
    public Optional<LoginChallenge> findForUpdate(String hashId) {
        return repository.findForUpdate(hashId).map(entity -> LoginChallenge.builder()
                .hashId(entity.getHashId()).userId(entity.getUserId()).codeHash(entity.getCodeHash())
                .expiresAt(entity.getExpiresAt()).createdAt(entity.getCreatedAt())
                .attempts(entity.getAttempts()).consumed(entity.isConsumed())
                .fingerprint(entity.getFingerprint()).userAgent(entity.getUserAgent())
                .email(entity.getEmail()).passwordHash(entity.getPasswordHash()).build());
    }
}
