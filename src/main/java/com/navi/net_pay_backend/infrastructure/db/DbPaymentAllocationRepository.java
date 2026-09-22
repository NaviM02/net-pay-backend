package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.entity.AppPayment;
import com.navi.net_pay_backend.domain.entity.BillingPeriod;
import com.navi.net_pay_backend.domain.entity.ClientCharge;
import com.navi.net_pay_backend.domain.entity.PaymentAllocation;
import com.navi.net_pay_backend.domain.repository.PaymentAllocationRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.BillingPeriodEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.ClientChargeEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.PaymentAllocationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DbPaymentAllocationRepository implements PaymentAllocationRepository {

    private final PaymentAllocationJpaRepository repository;
    private final AppPaymentJpaRepository paymentRepository;
    private final ClientChargeJpaRepository chargeRepository;

    @Override
    public BigDecimal sumAmountByChargeId(Long chargeId) {
        BigDecimal sum = repository.sumAmountByChargeId(chargeId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public List<PaymentAllocation> findByPaymentId(Long paymentId) {
        return repository.findByPaymentId(paymentId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteByPaymentId(Long paymentId) {
        repository.deleteByPaymentId(paymentId);
    }

    @Override
    public PaymentAllocation save(PaymentAllocation allocation) {
        PaymentAllocationEntity entity = new PaymentAllocationEntity();
        entity.setAmount(allocation.getAmount());
        entity.setCreatedAt(LocalDateTime.now());

        if (allocation.getPayment() != null && allocation.getPayment().getId() != null) {
            entity.setPayment(paymentRepository.getReferenceById(allocation.getPayment().getId()));
        }
        if (allocation.getCharge() != null && allocation.getCharge().getId() != null) {
            entity.setCharge(chargeRepository.getReferenceById(allocation.getCharge().getId()));
        }

        return toDomain(repository.save(entity));
    }

    private PaymentAllocation toDomain(PaymentAllocationEntity entity) {
        if (entity == null) return null;
        return PaymentAllocation.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .createdAt(entity.getCreatedAt())
                .payment(entity.getPayment() != null
                        ? AppPayment.builder().id(entity.getPayment().getId()).build()
                        : null)
                .charge(toDomain(entity.getCharge()))
                .build();
    }

    private ClientCharge toDomain(ClientChargeEntity entity) {
        if (entity == null) return null;
        return ClientCharge.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .expectedAmount(entity.getExpectedAmount())
                .period(toDomain(entity.getPeriod()))
                .build();
    }

    private BillingPeriod toDomain(BillingPeriodEntity entity) {
        if (entity == null) return null;
        return BillingPeriod.builder()
                .id(entity.getId())
                .year(entity.getYear())
                .month(entity.getMonth())
                .startsOn(entity.getStartsOn())
                .endsOn(entity.getEndsOn())
                .closed(entity.getClosed())
                .build();
    }
}
