package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.entity.AuditLog;
import com.navi.net_pay_backend.domain.repository.AuditLogRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.AdmTypologyEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AuditLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DbAuditLogRepository implements AuditLogRepository {

    private final AuditLogJpaRepository repository;
    private final AdmTypologyJpaRepository typologyRepository;

    @Override
    public AuditLog save(AuditLog log) {
        AuditLogEntity entity = new AuditLogEntity();
        entity.setUserId(log.getUserId());
        entity.setUserEmail(log.getUserEmail());
        entity.setAction(log.getAction());
        entity.setDetail(log.getDetail());
        entity.setEntityType(log.getEntityType());
        entity.setEntityHashId(log.getEntityHashId());
        entity.setChanges(log.getChanges());
        entity.setIpAddress(log.getIpAddress());
        if (log.getLevelId() != null) {
            entity.setTpLevel(typologyRepository.findByInternalId(log.getLevelId())
                    .orElseThrow(() -> new com.navi.net_pay_backend.domain.exception.EntityNotFoundException("typology_not_found")));
        }
        entity.setCreatedAt(log.getCreatedAt() != null ? log.getCreatedAt() : LocalDateTime.now());

        return toDomain(repository.save(entity));
    }

    @Override
    public List<AuditLog> findByEntity(String entityType, String entityHashId) {
        return repository.findByEntityTypeAndEntityHashIdOrderByCreatedAtDesc(entityType, entityHashId).stream()
                .map(this::toDomain)
                .toList();
    }

    private AuditLog toDomain(AuditLogEntity entity) {
        if (entity == null) return null;
        return AuditLog.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .userEmail(entity.getUserEmail())
                .action(entity.getAction())
                .detail(entity.getDetail())
                .entityType(entity.getEntityType())
                .entityHashId(entity.getEntityHashId())
                .changes(entity.getChanges())
                .ipAddress(entity.getIpAddress())
                .levelId(entity.getTpLevel() != null ? entity.getTpLevel().getInternalId() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
