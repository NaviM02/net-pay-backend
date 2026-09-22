package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.entity.PaymentAllocation;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentAllocationRepository {
    BigDecimal sumAmountByChargeId(Long chargeId);

    List<PaymentAllocation> findByPaymentId(Long paymentId);

    void deleteByPaymentId(Long paymentId);

    PaymentAllocation save(PaymentAllocation allocation);
}
