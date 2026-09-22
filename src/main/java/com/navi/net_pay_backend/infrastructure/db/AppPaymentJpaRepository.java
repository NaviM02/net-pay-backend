package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.AppPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppPaymentJpaRepository extends JpaRepository<AppPaymentEntity, Long>, JpaSpecificationExecutor<AppPaymentEntity> {

    Optional<AppPaymentEntity> findByHashIdAndTpPaymentStatusInternalIdNot(String hashId, Long internalId);
}
