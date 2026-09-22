package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.AuditLogQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.entity.AuditLog;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogUseCase {

    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository userRepository;

    public PaginatedResult<AuditLog> findAll(AuditLogQueryDto queryDto) {
        return auditLogRepository.findAll(queryDto);
    }

    public List<AuditLog> findByEntity(String entityType, String entityHashId) {
        return auditLogRepository.findByEntity(entityType, entityHashId);
    }

    public List<AuditLog> findByUserHashId(String userHashId) {
        AppUser user = userRepository.findByPublicIdAndStatusNot(userHashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("user_not_found"));
        return auditLogRepository.findByUserId(user.getId());
    }
}
