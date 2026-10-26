package com.navi.net_pay_backend.domain.service;

import java.util.Map;

public interface AuditLogService {
    void record(String action, String detail, String entityType, String entityHashId, Map<String, Object> changes);
}
