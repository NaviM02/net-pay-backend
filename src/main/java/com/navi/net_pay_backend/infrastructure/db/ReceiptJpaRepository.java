package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.ReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptJpaRepository extends JpaRepository<ReceiptEntity, Long>, JpaSpecificationExecutor<ReceiptEntity> {

    Optional<ReceiptEntity> findByHashId(String hashId);

    Optional<ReceiptEntity> findByPaymentId(Long paymentId);
}
