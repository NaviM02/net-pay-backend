package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long>, JpaSpecificationExecutor<AuditLogEntity> {

    List<AuditLogEntity> findByEntityTypeAndEntityHashIdOrderByCreatedAtDesc(String entityType, String entityHashId);

    List<AuditLogEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
