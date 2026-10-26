package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.LoginChallengeEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface LoginChallengeJpaRepository extends JpaRepository<LoginChallengeEntity, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from LoginChallengeEntity c where c.hashId = :hashId")
    Optional<LoginChallengeEntity> findForUpdate(@Param("hashId") String hashId);

    Optional<LoginChallengeEntity> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
