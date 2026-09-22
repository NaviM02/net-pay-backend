package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.entity.BillingPeriod;
import com.navi.net_pay_backend.domain.repository.BillingPeriodRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.BillingPeriodEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DbBillingPeriodRepository implements BillingPeriodRepository {

    private final BillingPeriodJpaRepository repository;

    @Override
    public Optional<BillingPeriod> findByYearAndMonth(int year, int month) {
        return repository.findByYearAndMonth(year, month).map(this::toDomain);
    }

    @Override
    public BillingPeriod save(BillingPeriod period) {
        BillingPeriodEntity entity = period.getId() != null
                ? repository.findById(period.getId()).orElseGet(BillingPeriodEntity::new)
                : new BillingPeriodEntity();

        entity.setYear(period.getYear());
        entity.setMonth(period.getMonth());
        entity.setStartsOn(period.getStartsOn());
        entity.setEndsOn(period.getEndsOn());
        entity.setClosed(period.getClosed() != null ? period.getClosed() : false);

        if (period.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        return toDomain(repository.save(entity));
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
