package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.AppPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AppPaymentJpaRepository extends JpaRepository<AppPaymentEntity, Long>, JpaSpecificationExecutor<AppPaymentEntity> {
    boolean existsByImportKey(String importKey);

    @Override
    @EntityGraph(attributePaths = {"client", "client.tpStatus", "client.service", "client.service.tpType",
            "client.service.tpStatus", "client.collector", "client.collector.tpStatus",
            "service", "service.tpType", "service.tpStatus", "collector", "collector.tpStatus",
            "tpPaymentMethod", "tpPaymentStatus"})
    Page<AppPaymentEntity> findAll(Specification<AppPaymentEntity> spec, Pageable pageable);

    Optional<AppPaymentEntity> findByHashIdAndTpPaymentStatusInternalIdNot(String hashId, Long internalId);

    @Query("""
        select coalesce(sum(p.amount), 0) from AppPaymentEntity p
        where p.client.id = :clientId
          and p.tpPaymentStatus.internalId <> :excludeStatusId
        """)
    BigDecimal sumAmountByClientId(@Param("clientId") Long clientId,
                                   @Param("excludeStatusId") Long excludeStatusId);
}
