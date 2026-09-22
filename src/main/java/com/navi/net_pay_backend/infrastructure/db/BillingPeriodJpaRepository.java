package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.BillingPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BillingPeriodJpaRepository extends JpaRepository<BillingPeriodEntity, Long> {

    Optional<BillingPeriodEntity> findByYearAndMonth(Integer year, Integer month);
}
