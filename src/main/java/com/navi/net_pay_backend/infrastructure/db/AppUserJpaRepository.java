package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppUserJpaRepository extends JpaRepository<AppUserEntity, Long>, JpaSpecificationExecutor<AppUserEntity> {
    Optional<AppUserEntity> findByEmailAndTpStatusInternalId(String email, Long internalId);

    Optional<AppUserEntity> findByIdAndTpStatusInternalIdNot(Long id, Long internalId);

    Optional<AppUserEntity> findByHashIdAndTpStatusInternalIdNot(String hashId, Long internalId);

    boolean existsByEmailAndTpStatusInternalIdIn(String email, List<Long> internalIds);
}