package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserJpaRepository extends JpaRepository<AppUserEntity, Long>, JpaSpecificationExecutor<AppUserEntity> {
    @EntityGraph(attributePaths = {"tpStatus", "tpRole"})
    Optional<AppUserEntity> findByEmailAndTpStatusInternalId(String email, Long internalId);

    @Modifying
    @Transactional
    @Query("update AppUserEntity u set u.lastLoginAt = :now, u.updatedAt = :now where u.id = :id")
    void updateLastLogin(@Param("id") Long id, @Param("now") java.time.LocalDateTime now);

    Optional<AppUserEntity> findByIdAndTpStatusInternalIdNot(Long id, Long internalId);

    Optional<AppUserEntity> findByHashIdAndTpStatusInternalIdNot(String hashId, Long internalId);

    Optional<AppUserEntity> findByResetTokenHash(String resetTokenHash);

    boolean existsByEmailAndTpStatusInternalIdIn(String email, List<Long> internalIds);

    boolean existsByEmailAndTpStatusInternalIdNot(String email, Long internalId);
}
