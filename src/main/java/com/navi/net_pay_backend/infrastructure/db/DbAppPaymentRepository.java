package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.dto.AppPaymentQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppPayment;
import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AppPaymentRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.AdmTypologyEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppClientEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppPaymentEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppServiceEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppUserEntity;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

@Repository
@RequiredArgsConstructor
public class DbAppPaymentRepository implements AppPaymentRepository {

    private final AppPaymentJpaRepository repository;
    private final AppClientJpaRepository clientRepository;
    private final AppServiceJpaRepository serviceRepository;
    private final AppUserJpaRepository userRepository;
    private final AdmTypologyJpaRepository typologyRepository;

    @Override
    public boolean existsByImportKey(String importKey) {
        return repository.existsByImportKey(importKey);
    }

    @Override
    public Optional<AppPayment> findByHashIdAndStatusNot(String hashId, Long statusId) {
        return repository.findByHashIdAndTpPaymentStatusInternalIdNot(hashId, statusId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResult<AppPayment> findAll(AppPaymentQueryDto queryDto) {
        Specification<AppPaymentEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (queryDto.getSearch() != null && !queryDto.getSearch().isBlank()) {
                String term = "%" + queryDto.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("client").get("businessName")), term),
                        cb.like(cb.lower(root.get("client").get("contactName")), term),
                        cb.like(cb.lower(root.get("client").get("contactEmail")), term)
                ));
            }

            if (queryDto.getStatus() != null) {
                predicates.add(cb.equal(root.get("tpPaymentStatus").get("internalId"), queryDto.getStatus()));
            }

            if (queryDto.getMethod() != null) {
                predicates.add(cb.equal(root.get("tpPaymentMethod").get("internalId"), queryDto.getMethod()));
            }

            if (queryDto.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("paidAt"), queryDto.getFromDate()));
            }

            if (queryDto.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("paidAt"), queryDto.getToDate()));
            }

            if (queryDto.getClientHashId() != null && !queryDto.getClientHashId().isBlank()) {
                predicates.add(cb.equal(root.get("client").get("hashId"), queryDto.getClientHashId()));
            }

            if (queryDto.getCollectorId() != null) {
                predicates.add(cb.equal(root.get("collector").get("id"), queryDto.getCollectorId()));
            }

            if (queryDto.getExcludeStatus() != null) {
                predicates.add(cb.notEqual(root.get("tpPaymentStatus").get("internalId"), queryDto.getExcludeStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort.Direction direction = queryDto.isAsc()
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String orderColumn = "id";

        if ("amount".equals(queryDto.getColumnOrder())) {
            orderColumn = "amount";
        } else if ("paidAt".equals(queryDto.getColumnOrder())) {
            orderColumn = "paidAt";
        } else if ("createdAt".equals(queryDto.getColumnOrder())) {
            orderColumn = "createdAt";
        }

        Pageable pageable = PageRequest.of(
                queryDto.getOffset() / queryDto.getSize(),
                queryDto.getSize(),
                Sort.by(direction, orderColumn)
        );

        Page<AppPaymentEntity> page = repository.findAll(specification, pageable);

        var serviceIds = new HashSet<Long>();
        for (AppPaymentEntity payment : page.getContent()) {
            if (payment.getService() != null) serviceIds.add(payment.getService().getId());
            if (payment.getClient() != null && payment.getClient().getService() != null) {
                serviceIds.add(payment.getClient().getService().getId());
            }
        }
        Map<Long, Integer> counts = new HashMap<>();
        if (!serviceIds.isEmpty()) {
            for (Object[] row : clientRepository.countClientsByServiceIds(serviceIds, TypologyEnum.DELETED.getId())) {
                counts.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
            }
        }

        List<AppPayment> items = page.getContent()
                .stream()
                .map(entity -> toDomain(entity, counts))
                .toList();

        return new PaginatedResult<>(items, page.getTotalElements());
    }

    @Override
    public BigDecimal sumAmountByClientId(Long clientId, Long excludeStatusId) {
        BigDecimal sum = repository.sumAmountByClientId(clientId, excludeStatusId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public AppPayment save(AppPayment payment) {
        AppPaymentEntity entity = payment.getId() != null
                ? repository.findById(payment.getId()).orElseGet(AppPaymentEntity::new)
                : new AppPaymentEntity();

        entity.setHashId(payment.getHashId());
        entity.setImportKey(payment.getImportKey());
        entity.setAmount(payment.getAmount());
        entity.setPaidAt(payment.getPaidAt());
        entity.setReference(payment.getReference());
        entity.setAttachmentUrl(payment.getAttachmentUrl());
        entity.setComment(payment.getComment());
        entity.setDeletedAt(payment.getDeletedAt());

        if (payment.getTpPaymentMethod() != null) {
            entity.setTpPaymentMethod(resolveTypology(payment.getTpPaymentMethod()));
        }
        if (payment.getTpPaymentStatus() != null) {
            entity.setTpPaymentStatus(resolveTypology(payment.getTpPaymentStatus()));
        }

        if (payment.getClient() != null) {
            entity.setClient(clientRepository.findById(payment.getClient().getId())
                    .orElseThrow(() -> new EntityNotFoundException("payment_client_not_found")));
        }

        if (payment.getService() != null) {
            entity.setService(serviceRepository.findById(payment.getService().getId())
                    .orElseThrow(() -> new EntityNotFoundException("payment_service_not_found")));
        }

        if (payment.getCollector() != null) {
            entity.setCollector(userRepository.findById(payment.getCollector().getId())
                    .orElseThrow(() -> new EntityNotFoundException("payment_collector_not_found")));
        } else {
            entity.setCollector(null);
        }

        if (payment.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        AppPaymentEntity savedEntity = repository.save(entity);
        return toDomain(savedEntity);
    }

    private AdmTypologyEntity resolveTypology(AdmTypology typology) {
        Long key = typology.getInternalId() != null ? typology.getInternalId() : typology.getTypologyId();
        return typologyRepository.findByInternalId(key)
                .orElseThrow(() -> new EntityNotFoundException("typology_not_found"));
    }

    private AppPayment toDomain(AppPaymentEntity entity) {
        return toDomain(entity, null);
    }

    private AppPayment toDomain(AppPaymentEntity entity, Map<Long, Integer> counts) {
        if (entity == null) return null;

        return AppPayment.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .importKey(entity.getImportKey())
                .amount(entity.getAmount())
                .paidAt(entity.getPaidAt())
                .reference(entity.getReference())
                .attachmentUrl(entity.getAttachmentUrl())
                .comment(entity.getComment())
                .client(toDomain(entity.getClient(), counts))
                .service(toDomain(entity.getService(), counts))
                .collector(toDomain(entity.getCollector()))
                .tpPaymentMethod(toDomain(entity.getTpPaymentMethod()))
                .tpPaymentStatus(toDomain(entity.getTpPaymentStatus()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    private AppClient toDomain(AppClientEntity entity, Map<Long, Integer> counts) {
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
                .service(toDomain(entity.getService(), counts))
                .collector(toDomain(entity.getCollector()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    private AppService toDomain(AppServiceEntity entity, Map<Long, Integer> counts) {
        if (entity == null) return null;
        return AppService.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .name(entity.getName())
                .speed(entity.getSpeed())
                .monthlyPrice(entity.getMonthlyPrice())
                .clientCount(counts != null ? counts.getOrDefault(entity.getId(), 0)
                        : (int) clientRepository.countByServiceIdAndTpStatusInternalIdNot(
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
