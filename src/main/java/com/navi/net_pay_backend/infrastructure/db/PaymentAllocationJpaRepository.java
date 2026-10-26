package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.PaymentAllocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PaymentAllocationJpaRepository extends JpaRepository<PaymentAllocationEntity, Long> {

    @Query("select coalesce(sum(a.amount), 0) from PaymentAllocationEntity a where a.charge.id = :chargeId")
    BigDecimal sumAmountByChargeId(@Param("chargeId") Long chargeId);

    List<PaymentAllocationEntity> findByPaymentId(Long paymentId);

    void deleteByPaymentId(Long paymentId);
}
