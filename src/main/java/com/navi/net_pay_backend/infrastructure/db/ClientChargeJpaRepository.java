package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.ClientChargeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientChargeJpaRepository extends JpaRepository<ClientChargeEntity, Long> {

    Optional<ClientChargeEntity> findByClientIdAndPeriodId(Long clientId, Long periodId);

    Optional<ClientChargeEntity> findByHashId(String hashId);

    List<ClientChargeEntity> findByClientIdOrderByPeriodYearAscPeriodMonthAsc(Long clientId);

    Page<ClientChargeEntity> findByClientIdAndDeletedAtIsNullOrderByPeriodYearAscPeriodMonthAscIdAsc(Long clientId, Pageable pageable);

    long countByClientIdAndDeletedAtIsNull(Long clientId);
}
