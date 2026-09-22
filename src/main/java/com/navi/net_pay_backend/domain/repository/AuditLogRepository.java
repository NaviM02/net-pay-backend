package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.dto.AuditLogQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AuditLog;

import java.util.List;

public interface AuditLogRepository {
    AuditLog save(AuditLog log);

    PaginatedResult<AuditLog> findAll(AuditLogQueryDto queryDto);

    List<AuditLog> findByEntity(String entityType, String entityHashId);

    List<AuditLog> findByUserId(Long userId);
}
