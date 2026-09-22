package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.dto.AppUserQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.AdmTypologyEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppUserEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class DbAppUserRepository implements AppUserRepository {

    private final AppUserJpaRepository repository;

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
        Specification<AppUserEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (queryDto.getIds() != null && !queryDto.getIds().isEmpty()) {
                predicates.add(root.get("id").in(queryDto.getIds()));
            }

            if (queryDto.getEmail() != null && !queryDto.getEmail().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("email")), "%" + queryDto.getEmail().toLowerCase() + "%"
                ));
            }

            if (queryDto.getFullName() != null && !queryDto.getFullName().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("fullName")), "%" + queryDto.getFullName().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort.Direction direction = queryDto.isAsc()
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String orderColumn = "id";

        if ("email".equals(queryDto.getColumnOrder())) {
            orderColumn = "email";
        } else if ("fullName".equals(queryDto.getColumnOrder())) {
            orderColumn = "fullName";
        }

        Pageable pageable = PageRequest.of(
                queryDto.getOffset() / queryDto.getSize(),
                queryDto.getSize(),
                Sort.by(direction, orderColumn)
        );

        Page<AppUserEntity> page = repository.findAll(specification, pageable);

        List<AppUser> items = page.getContent()
                .stream()
                .map(this::toDomain)
                .toList();

        return new PaginatedResult<>(items, page.getTotalElements());
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
        entity.setPassword(user.getPassword());

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
                .password(entity.getPassword())
                .tpStatus(statusDto)
                .tpRole(roleDto)
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }
}