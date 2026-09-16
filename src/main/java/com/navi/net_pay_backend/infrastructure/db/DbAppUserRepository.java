package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.dto.AppUserQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.AdmTypologyEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppUserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DbAppUserRepository implements AppUserRepository {

    private final AppUserJpaRepository repository;
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Optional<AppUser> findByEmailAndStatus(String email, Long statusId) {
        // En tu JpaRepository debes actualizar para buscar por tpStatus.typologyId
        return repository.findByEmailAndTpStatusInternalId(email, statusId).map(this::toDomain);
    }

    @Override
    public void updateLastLogin(Long userId) {
        AppUserEntity user = repository.findById(userId).orElseThrow();
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        repository.save(user);
    }

    @Override
    public Optional<AppUser> findByIdAndStatusNot(Long id, Long statusId) {
        return repository.findByIdAndTpStatusInternalIdNot(id, statusId).map(this::toDomain);
    }

    @Override
    public Optional<AppUser> findByPublicIdAndStatusNot(String publicId, Long statusId) {
        return repository.findByHashIdAndTpStatusInternalIdNot(publicId, statusId).map(this::toDomain);
    }

    @Override
    public PaginatedResult<AppUser> findAll(AppUserQueryDto queryDto) {
        StringBuilder whereStmt = new StringBuilder(" WHERE 1 = 1");
        Map<String, Object> binds = new HashMap<>();

        if (queryDto.getIds() != null && !queryDto.getIds().isEmpty()) {
            whereStmt.append(" AND u.id IN (:ids)");
            binds.put("ids", queryDto.getIds());
        }
        if (queryDto.getEmail() != null && !queryDto.getEmail().isBlank()) {
            whereStmt.append(" AND LOWER(u.email) LIKE :email");
            binds.put("email", "%" + queryDto.getEmail().toLowerCase() + "%");
        }
        if (queryDto.getFullName() != null && !queryDto.getFullName().isBlank()) {
            whereStmt.append(" AND LOWER(u.full_name) LIKE :fullName");
            binds.put("fullName", "%" + queryDto.getFullName().toLowerCase() + "%");
        }

        String countQueryStr = "SELECT COUNT(*) FROM tc_user u" + whereStmt;
        Query countQuery = entityManager.createNativeQuery(countQueryStr);
        binds.forEach(countQuery::setParameter);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) return new PaginatedResult<>(List.of(), 0);

        Map<String, String> colMap = Map.of(
            "id", "u.id",
            "email", "u.email",
            "fullName", "u.full_name"
        );
        String orderColumn = colMap.getOrDefault(queryDto.getColumnOrder(), "u.id");
        String direction = queryDto.isAsc() ? "ASC" : "DESC";
        whereStmt.append(" ORDER BY ").append(orderColumn).append(" ").append(direction);

        String selectQueryStr = "SELECT * FROM tc_user u" + whereStmt;
        Query selectQuery = entityManager.createNativeQuery(selectQueryStr, AppUserEntity.class); // Mapea a la Entidad
        binds.forEach(selectQuery::setParameter);

        if (queryDto.getOffset() != null && queryDto.getSize() != null && queryDto.getSize() > 0) {
            selectQuery.setFirstResult(queryDto.getOffset());
            selectQuery.setMaxResults(queryDto.getSize());
        }

        @SuppressWarnings("unchecked")
        List<AppUserEntity> entities = selectQuery.getResultList();
        List<AppUser> items = entities.stream().map(this::toDomain).toList();
        return new PaginatedResult<>(items, total);
    }

    @Override
    public boolean existsByEmailAndStatusIn(String email, List<Long> statusIds) {
        return repository.existsByEmailAndTpStatusInternalIdIn(email, statusIds);
    }

    @Override
    public AppUser save(AppUser user) {
        AppUserEntity entity = user.getId() != null
                ? repository.findById(user.getId()).orElseGet(AppUserEntity::new)
                : new AppUserEntity();

        entity.setHashId(user.getHashId());
        entity.setFullName(user.getFullName());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());

        // Asignamos las referencias lógicas mediante IDs simulando el comportamiento sin constraints
        if (user.getTpStatus() != null) {
            entity.setTpStatus(AdmTypologyEntity.builder().typologyId(user.getTpStatus().getTypologyId()).build());
        }
        if (user.getTpRole() != null) {
            entity.setTpRole(AdmTypologyEntity.builder().typologyId(user.getTpRole().getTypologyId()).build());
        }

        if (user.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        AppUserEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    private AppUser toDomain(AppUserEntity entity) {
        if (entity == null) return null;

        AdmTypology statusDto = entity.getTpStatus() != null ? AdmTypology.builder()
                .typologyId(entity.getTpStatus().getTypologyId())
                .internalId(entity.getTpStatus().getInternalId())
                .parentTypologyId(entity.getTpStatus().getParentTypologyId())
                .description(entity.getTpStatus().getDescription())
                .value1(entity.getTpStatus().getValue1())
                .value2(entity.getTpStatus().getValue2())
                .build() : null;

        AdmTypology roleDto = entity.getTpRole() != null ? AdmTypology.builder()
                .typologyId(entity.getTpRole().getTypologyId())
                .internalId(entity.getTpRole().getInternalId())
                .parentTypologyId(entity.getTpRole().getParentTypologyId())
                .description(entity.getTpRole().getDescription())
                .value1(entity.getTpRole().getValue1())
                .value2(entity.getTpRole().getValue2())
                .build() : null;

        return AppUser.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .tpStatus(statusDto)
                .tpRole(roleDto)
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }
}