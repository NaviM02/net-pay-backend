package com.navi.net_pay_backend.infrastructure.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogResponseDto {
    private Long id;
    private Long userId;
    private String userEmail;
    private String action;
    private String detail;
    private String entityType;
    private String entityHashId;
    private Map<String, Object> changes;
    private String ipAddress;
    private Long level;
    private String levelName;
    private LocalDateTime createdAt;
}
