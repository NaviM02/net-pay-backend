package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.dto.AuditLogQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AuditLog;
import com.navi.net_pay_backend.domain.repository.AuditLogRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.AuditLogEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public PaginatedResult<AuditLog> findAll(AuditLogQueryDto queryDto) {
        Specification<AuditLogEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (queryDto.getSearch() != null && !queryDto.getSearch().isBlank()) {
                String term = "%" + queryDto.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("action")), term),
                        cb.like(cb.lower(root.get("detail")), term),
                        cb.like(cb.lower(root.get("userEmail")), term)
                ));
            }

            if (queryDto.getLevel() != null) {
                predicates.add(cb.equal(root.get("levelId"), queryDto.getLevel()));
            }

            if (queryDto.getUserId() != null) {
                predicates.add(cb.equal(root.get("userId"), queryDto.getUserId()));
            }

            if (queryDto.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), queryDto.getFromDate()));
            }

            if (queryDto.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), queryDto.getToDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort.Direction direction = queryDto.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(
                queryDto.getOffset() / queryDto.getSize(),
                queryDto.getSize(),
                Sort.by(direction, "createdAt")
        );

        Page<AuditLogEntity> page = repository.findAll(specification, pageable);
        List<AuditLog> items = page.getContent().stream().map(this::toDomain).toList();
        return new PaginatedResult<>(items, page.getTotalElements());
    }

    @Override
    public List<AuditLog> findByEntity(String entityType, String entityHashId) {
        return repository.findByEntityTypeAndEntityHashIdOrderByCreatedAtDesc(entityType, entityHashId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<AuditLog> findByUserId(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
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
