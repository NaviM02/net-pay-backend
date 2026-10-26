package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.entity.BillingPeriod;

import java.util.Optional;

public interface BillingPeriodRepository {
    Optional<BillingPeriod> findByYearAndMonth(int year, int month);

    BillingPeriod save(BillingPeriod period);
}
