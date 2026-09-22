package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.dto.AppReceiptQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.AppPayment;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.entity.BillingPeriod;
import com.navi.net_pay_backend.domain.entity.Receipt;
import com.navi.net_pay_backend.domain.repository.ReceiptRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.AdmTypologyEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppClientEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppPaymentEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppUserEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.BillingPeriodEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.ReceiptEntity;
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
public class DbReceiptRepository implements ReceiptRepository {

    private final ReceiptJpaRepository repository;
    private final AppPaymentJpaRepository paymentRepository;
    private final AppClientJpaRepository clientRepository;
    private final AppUserJpaRepository userRepository;
    private final BillingPeriodJpaRepository periodRepository;

    @Override
    public Optional<Receipt> findByHashId(String hashId) {
        return repository.findByHashId(hashId).map(this::toDomain);
    }

    @Override
    public Optional<Receipt> findByPaymentId(Long paymentId) {
        return repository.findByPaymentId(paymentId).map(this::toDomain);
    }

    @Override
    public PaginatedResult<Receipt> findAll(AppReceiptQueryDto queryDto) {
        Specification<ReceiptEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (queryDto.getSearch() != null && !queryDto.getSearch().isBlank()) {
                String term = "%" + queryDto.getSearch().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("receiptNumber")), term),
                        cb.like(cb.lower(root.get("client").get("businessName")), term),
                        cb.like(cb.lower(root.get("client").get("contactName")), term)
                ));
            }

            if (queryDto.getPeriodId() != null) {
                predicates.add(cb.equal(root.get("period").get("id"), queryDto.getPeriodId()));
            }

            if (queryDto.getClientHashId() != null && !queryDto.getClientHashId().isBlank()) {
                predicates.add(cb.equal(root.get("client").get("hashId"), queryDto.getClientHashId()));
            }

            if (queryDto.getExcludeStatus() != null) {
                predicates.add(cb.notEqual(root.get("payment").get("tpPaymentStatus").get("internalId"), queryDto.getExcludeStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort.Direction direction = queryDto.isAsc() ? Sort.Direction.ASC : Sort.Direction.DESC;
        String orderColumn = "id";
        if ("receiptNumber".equals(queryDto.getColumnOrder())) {
            orderColumn = "receiptNumber";
        } else if ("issuedAt".equals(queryDto.getColumnOrder())) {
            orderColumn = "issuedAt";
        } else if ("amount".equals(queryDto.getColumnOrder())) {
            orderColumn = "amount";
        }

        Pageable pageable = PageRequest.of(
                queryDto.getOffset() / queryDto.getSize(),
                queryDto.getSize(),
                Sort.by(direction, orderColumn)
        );

        Page<ReceiptEntity> page = repository.findAll(specification, pageable);
        List<Receipt> items = page.getContent().stream().map(this::toDomain).toList();
        return new PaginatedResult<>(items, page.getTotalElements());
    }

    @Override
    public Receipt save(Receipt receipt) {
        ReceiptEntity entity = receipt.getId() != null
                ? repository.findById(receipt.getId()).orElseGet(ReceiptEntity::new)
                : new ReceiptEntity();

        entity.setHashId(receipt.getHashId());
        entity.setReceiptNumber(receipt.getReceiptNumber());
        entity.setAmount(receipt.getAmount());
        entity.setIssuedAt(receipt.getIssuedAt());
        entity.setDocumentPath(receipt.getDocumentPath());
        entity.setPublicId(receipt.getPublicId());
        entity.setPrintedAt(receipt.getPrintedAt());
        entity.setDeletedAt(receipt.getDeletedAt());

        if (receipt.getPayment() != null && receipt.getPayment().getId() != null) {
            entity.setPayment(paymentRepository.findById(receipt.getPayment().getId())
                    .orElseThrow(() -> new com.navi.net_pay_backend.domain.exception.EntityNotFoundException("receipt_payment_not_found")));
        }
        if (receipt.getClient() != null && receipt.getClient().getId() != null) {
            entity.setClient(clientRepository.findById(receipt.getClient().getId())
                    .orElseThrow(() -> new com.navi.net_pay_backend.domain.exception.EntityNotFoundException("receipt_client_not_found")));
        }
        if (receipt.getPeriod() != null && receipt.getPeriod().getId() != null) {
            entity.setPeriod(periodRepository.findById(receipt.getPeriod().getId())
                    .orElseThrow(() -> new com.navi.net_pay_backend.domain.exception.EntityNotFoundException("receipt_period_not_found")));
        }
        if (receipt.getCollector() != null && receipt.getCollector().getId() != null) {
            entity.setCollector(userRepository.findById(receipt.getCollector().getId())
                    .orElseThrow(() -> new com.navi.net_pay_backend.domain.exception.EntityNotFoundException("receipt_collector_not_found")));
        }

        if (receipt.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        return toDomain(repository.save(entity));
    }

    private Receipt toDomain(ReceiptEntity entity) {
        if (entity == null) return null;
        return Receipt.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .receiptNumber(entity.getReceiptNumber())
                .amount(entity.getAmount())
                .issuedAt(entity.getIssuedAt())
                .documentPath(entity.getDocumentPath())
                .publicId(entity.getPublicId())
                .printedAt(entity.getPrintedAt())
                .payment(toDomain(entity.getPayment()))
                .client(toDomain(entity.getClient()))
                .period(toDomain(entity.getPeriod()))
                .collector(toDomain(entity.getCollector()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    private AppPayment toDomain(AppPaymentEntity entity) {
        if (entity == null) return null;
        return AppPayment.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .amount(entity.getAmount())
                .paidAt(entity.getPaidAt())
                .reference(entity.getReference())
                .tpPaymentMethod(toDomain(entity.getTpPaymentMethod()))
                .build();
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
                .build();
    }

    private AppUser toDomain(AppUserEntity entity) {
        if (entity == null) return null;
        return AppUser.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .build();
    }

    private BillingPeriod toDomain(BillingPeriodEntity entity) {
        if (entity == null) return null;
        return BillingPeriod.builder()
                .id(entity.getId())
                .year(entity.getYear())
                .month(entity.getMonth())
                .startsOn(entity.getStartsOn())
                .endsOn(entity.getEndsOn())
                .build();
    }

    @SuppressWarnings("unused")
    private AdmTypology toDomain(AdmTypologyEntity entity) {
        if (entity == null) return null;
        return AdmTypology.builder()
                .typologyId(entity.getTypologyId())
                .internalId(entity.getInternalId())
                .parentTypologyId(entity.getParentTypologyId())
                .description(entity.getDescription())
                .build();
    }
}
