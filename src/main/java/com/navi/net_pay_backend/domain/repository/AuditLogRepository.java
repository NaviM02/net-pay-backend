package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.entity.AuditLog;

import java.util.List;

public interface AuditLogRepository {
    AuditLog save(AuditLog log);

    List<AuditLog> findByEntity(String entityType, String entityHashId);
}
