package com.navi.net_pay_backend.domain.entity;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class AuditLog {
    private Long id;
    private Long userId;
    private String userEmail;
    private String action;
    private String detail;
    private String entityType;
    private String entityHashId;
    private Map<String, Object> changes;
    private String ipAddress;
    private Long levelId;
    private LocalDateTime createdAt;
}
