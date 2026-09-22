package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.dto.AppServiceQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AppServiceRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.AdmTypologyEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppServiceEntity;
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
public class DbAppServiceRepository implements AppServiceRepository {

    private final AppServiceJpaRepository repository;
    private final AdmTypologyJpaRepository typologyRepository;

    @Override
    public Optional<AppService> findByHashIdAndStatusNot(String hashId, Long statusId) {
        return repository.findByHashIdAndTpStatusInternalIdNot(hashId, statusId).map(this::toDomain);
    }

    @Override
    public PaginatedResult<AppService> findAll(AppServiceQueryDto queryDto) {
        Specification<AppServiceEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (queryDto.getIds() != null && !queryDto.getIds().isEmpty()) {
                predicates.add(root.get("id").in(queryDto.getIds()));
            }

            if (queryDto.getName() != null && !queryDto.getName().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("name")), "%" + queryDto.getName().toLowerCase() + "%"
                ));
            }

            if (queryDto.getSpeed() != null && !queryDto.getSpeed().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("speed")), "%" + queryDto.getSpeed().toLowerCase() + "%"
                ));
            }

            if (queryDto.getType() != null) {
                predicates.add(cb.equal(root.join("tpType").get("internalId"), queryDto.getType()));
            }

            if (queryDto.getStatus() != null) {
                predicates.add(cb.equal(root.join("tpStatus").get("internalId"), queryDto.getStatus()));
            }

            if (queryDto.getExcludeStatus() != null) {
                predicates.add(cb.notEqual(root.join("tpStatus").get("internalId"), queryDto.getExcludeStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort.Direction direction = queryDto.isAsc()
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String orderColumn = "id";

        if ("name".equals(queryDto.getColumnOrder())) {
            orderColumn = "name";
        } else if ("speed".equals(queryDto.getColumnOrder())) {
            orderColumn = "speed";
        } else if ("monthlyPrice".equals(queryDto.getColumnOrder())) {
            orderColumn = "monthlyPrice";
        } else if ("clientCount".equals(queryDto.getColumnOrder())) {
            orderColumn = "clientCount";
        }

        Pageable pageable = PageRequest.of(
                queryDto.getOffset() / queryDto.getSize(),
                queryDto.getSize(),
                Sort.by(direction, orderColumn)
        );

        Page<AppServiceEntity> page = repository.findAll(specification, pageable);

        List<AppService> items = page.getContent()
                .stream()
                .map(this::toDomain)
                .toList();

        return new PaginatedResult<>(items, page.getTotalElements());
    }

    @Override
    public boolean existsByNameAndDeletedAtIsNull(String name) {
        return repository.existsByNameIgnoreCaseAndDeletedAtIsNull(name);
    }

    @Override
    public boolean existsByNameAndDeletedAtIsNullAndIdNot(String name, Long id) {
        return repository.existsByNameIgnoreCaseAndDeletedAtIsNullAndIdNot(name, id);
    }

    @Override
    public AppService save(AppService service) {
        AppServiceEntity entity = service.getId() != null
                ? repository.findById(service.getId()).orElseGet(AppServiceEntity::new)
                : new AppServiceEntity();

        entity.setHashId(service.getHashId());
        entity.setName(service.getName());
        entity.setSpeed(service.getSpeed());
        entity.setMonthlyPrice(service.getMonthlyPrice());
        entity.setDeletedAt(service.getDeletedAt());

        if (service.getTpType() != null) {
            entity.setTpType(resolveTypology(service.getTpType()));
        }
        if (service.getTpStatus() != null) {
            entity.setTpStatus(resolveTypology(service.getTpStatus()));
        }

        if (service.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        AppServiceEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    private AdmTypologyEntity resolveTypology(AdmTypology typology) {
        Long key = typology.getInternalId() != null ? typology.getInternalId() : typology.getTypologyId();
        return typologyRepository.findByInternalId(key)
                .orElseThrow(() -> new EntityNotFoundException("typology_not_found"));
    }

    private AppService toDomain(AppServiceEntity entity) {
        if (entity == null) return null;

        AdmTypology typeDto = toDomain(entity.getTpType());
        AdmTypology statusDto = toDomain(entity.getTpStatus());

        return AppService.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .name(entity.getName())
                .speed(entity.getSpeed())
                .monthlyPrice(entity.getMonthlyPrice())
                .tpType(typeDto)
                .tpStatus(statusDto)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    private AdmTypology toDomain(AdmTypologyEntity entity) {
        if (entity == null) return null;
        return AdmTypology.builder()
                .typologyId(entity.getTypologyId())
                .internalId(entity.getInternalId())
                .parentTypologyId(entity.getParentTypologyId())
                .description(entity.getDescription())
                .value1(entity.getValue1())
                .value2(entity.getValue2())
                .build();
    }
}
