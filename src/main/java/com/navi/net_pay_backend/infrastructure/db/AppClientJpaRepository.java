package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.AppClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppClientJpaRepository extends JpaRepository<AppClientEntity, Long>, JpaSpecificationExecutor<AppClientEntity> {

    Optional<AppClientEntity> findByHashIdAndTpStatusInternalIdNot(String hashId, Long internalId);

    long countByServiceIdAndTpStatusInternalIdNot(Long serviceId, Long internalId);
}
