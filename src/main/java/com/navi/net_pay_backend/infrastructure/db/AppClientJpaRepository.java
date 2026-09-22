package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.AppClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppClientJpaRepository extends JpaRepository<AppClientEntity, Long>, JpaSpecificationExecutor<AppClientEntity> {

    List<AppClientEntity> findByTpStatusInternalIdNot(Long internalId);

    List<AppClientEntity> findByCollectorIdAndTpStatusInternalIdNot(Long collectorId, Long internalId);

    Optional<AppClientEntity> findByIdAndTpStatusInternalIdNot(Long id, Long internalId);

    Optional<AppClientEntity> findByHashIdAndTpStatusInternalIdNot(String hashId, Long internalId);

    long countByServiceIdAndTpStatusInternalIdNot(Long serviceId, Long internalId);
}
