package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.entity.AuditLog;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.repository.AuditLogRepository;
import com.navi.net_pay_backend.domain.service.AuditLogService;
import com.navi.net_pay_backend.domain.service.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository userRepository;
    private final SecurityContext securityContext;

    @Override
    public void record(String action, String detail, String entityType, String entityHashId, Map<String, Object> changes) {
        Long userId = securityContext.getUserId().orElse(null);
        String userEmail = userId != null
                ? userRepository.findByIdAndStatusNot(userId, TypologyEnum.DELETED.getId())
                        .map(user -> user.getEmail())
                        .orElse(null)
                : null;

        auditLogRepository.save(AuditLog.builder()
                .userId(userId)
                .userEmail(userEmail)
                .action(action)
                .detail(detail)
                .entityType(entityType)
                .entityHashId(entityHashId)
                .changes(changes)
                .levelId(TypologyEnum.INFO.getId())
                .createdAt(LocalDateTime.now())
                .build());
    }
}
