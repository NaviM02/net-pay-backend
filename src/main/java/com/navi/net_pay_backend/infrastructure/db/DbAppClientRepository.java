package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.dto.AppClientQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AppClientRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.AdmTypologyEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppServiceEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppUserEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppClientEntity;
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
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DbAppClientRepository implements AppClientRepository {

    private final AppClientJpaRepository repository;
    private final AppServiceJpaRepository serviceRepository;
    private final AppUserJpaRepository userRepository;
    private final AdmTypologyJpaRepository typologyRepository;

    @Override
    public Optional<AppClient> findByIdAndStatusNot(Long id, Long statusId) {
        return repository.findByIdAndTpStatusInternalIdNot(id, statusId).map(this::toDomain);
    }

    @Override
    public Optional<AppClient> findByHashIdAndStatusNot(String hashId, Long statusId) {
        return repository.findByHashIdAndTpStatusInternalIdNot(hashId, statusId).map(this::toDomain);
    }

    @Override
    public PaginatedResult<AppClient> findAll(AppClientQueryDto queryDto) {
        Specification<AppClientEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (queryDto.getIds() != null && !queryDto.getIds().isEmpty()) {
                predicates.add(root.get("id").in(queryDto.getIds()));
            }

            if (queryDto.getSearch() != null && !queryDto.getSearch().isBlank()) {
                String term = "%" + queryDto.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("businessName")), term),
                        cb.like(cb.lower(root.get("contactName")), term),
                        cb.like(cb.lower(root.get("contactEmail")), term)
                ));
            }

            if (queryDto.getStatus() != null) {
                predicates.add(cb.equal(root.get("tpStatus").get("internalId"), queryDto.getStatus()));
            }

            if (queryDto.getServiceId() != null) {
                predicates.add(cb.equal(root.get("service").get("id"), queryDto.getServiceId()));
            }

            if (queryDto.getCollectorId() != null) {
                predicates.add(cb.equal(root.get("collector").get("id"), queryDto.getCollectorId()));
            }

            if (queryDto.getExcludeStatus() != null) {
                predicates.add(cb.notEqual(root.get("tpStatus").get("internalId"), queryDto.getExcludeStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort.Direction direction = queryDto.isAsc()
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String orderColumn = "id";

        if ("businessName".equals(queryDto.getColumnOrder())) {
            orderColumn = "businessName";
        } else if ("contactName".equals(queryDto.getColumnOrder())) {
            orderColumn = "contactName";
        } else if ("customerSince".equals(queryDto.getColumnOrder())) {
            orderColumn = "customerSince";
        } else if ("createdAt".equals(queryDto.getColumnOrder())) {
            orderColumn = "createdAt";
        }

        Pageable pageable = PageRequest.of(
                queryDto.getOffset() / queryDto.getSize(),
                queryDto.getSize(),
                Sort.by(direction, orderColumn)
        );

        Page<AppClientEntity> page = repository.findAll(specification, pageable);

        List<AppClient> items = page.getContent()
                .stream()
                .map(this::toDomain)
                .toList();

        return new PaginatedResult<>(items, page.getTotalElements());
    }

    @Override
    public long countByServiceIdAndStatusNot(Long serviceId, Long statusId) {
        return repository.countByServiceIdAndTpStatusInternalIdNot(serviceId, statusId);
    }

    @Override
    public AppClient save(AppClient client) {
        AppClientEntity entity = client.getId() != null
                ? repository.findById(client.getId()).orElseGet(AppClientEntity::new)
                : new AppClientEntity();

        entity.setHashId(client.getHashId());
        entity.setBusinessName(client.getBusinessName());
        entity.setContactName(client.getContactName());
        entity.setContactEmail(client.getContactEmail());
        entity.setContactPhone(client.getContactPhone());
        entity.setAddress(client.getAddress());
        entity.setLatitude(client.getLatitude());
        entity.setLongitude(client.getLongitude());
        entity.setCustomerSince(client.getCustomerSince());
        entity.setDeletedAt(client.getDeletedAt());

        if (client.getTpStatus() != null) {
            entity.setTpStatus(resolveTypology(client.getTpStatus()));
        }
        if (client.getService() != null) {
            entity.setService(serviceRepository.findById(client.getService().getId())
                    .orElseThrow(() -> new EntityNotFoundException("client_service_not_found")));
        }

        if (client.getCollector() != null) {
            entity.setCollector(userRepository.findById(client.getCollector().getId())
                    .orElseThrow(() -> new EntityNotFoundException("client_collector_not_found")));
        } else {
            entity.setCollector(null);
        }

        if (client.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        AppClientEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    private AdmTypologyEntity resolveTypology(AdmTypology typology) {
        Long key = typology.getInternalId() != null ? typology.getInternalId() : typology.getTypologyId();
        return typologyRepository.findByInternalId(key)
                .orElseThrow(() -> new EntityNotFoundException("typology_not_found"));
    }

    private AppClient toDomain(AppClientEntity entity) {
        if (entity == null) return null;

        return AppClient.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .businessName(entity.getBusinessName())
                .contactName(entity.getContactName())
                .contactEmail(entity.getContactEmail())
                .contactPhone(entity.getContactPhone())
                .address(entity.getAddress())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .customerSince(entity.getCustomerSince())
                .tpStatus(toDomain(entity.getTpStatus()))
                .service(toDomain(entity.getService()))
                .collector(toDomain(entity.getCollector()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    private AppService toDomain(AppServiceEntity entity) {
        if (entity == null) return null;
        return AppService.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .name(entity.getName())
                .speed(entity.getSpeed())
                .monthlyPrice(entity.getMonthlyPrice())
                .clientCount((int) repository.countByServiceIdAndTpStatusInternalIdNot(
                        entity.getId(), TypologyEnum.DELETED.getId()))
                .tpType(toDomain(entity.getTpType()))
                .tpStatus(toDomain(entity.getTpStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    private AppUser toDomain(AppUserEntity entity) {
        if (entity == null) return null;
        return AppUser.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .tpRole(toDomain(entity.getTpRole()))
                .tpStatus(toDomain(entity.getTpStatus()))
                .lastLoginAt(entity.getLastLoginAt())
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
