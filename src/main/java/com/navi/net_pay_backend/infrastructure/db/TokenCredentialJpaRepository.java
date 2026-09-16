package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.TokenCredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenCredentialJpaRepository extends JpaRepository<TokenCredentialEntity, Long> {

    @Modifying
    @Query("""
        DELETE FROM TokenCredentialEntity t
        WHERE t.userId = :userId
        AND t.fingerprint = :fingerprint
    """)
    void deleteByUserIdAndFingerprint(@Param("userId") Long userId, @Param("fingerprint") String fingerprint);

    boolean existsByTokenAndExpirationDateAfter(String token, LocalDateTime expirationDate);

    Optional<TokenCredentialEntity> findFirstByUserIdOrderByIdDesc(Long userId);
}