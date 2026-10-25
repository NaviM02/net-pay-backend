package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.AppPaymentQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.AppPayment;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.entity.ClientCharge;
import com.navi.net_pay_backend.domain.entity.PaymentAllocation;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AppClientRepository;
import com.navi.net_pay_backend.domain.repository.AppPaymentRepository;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.repository.ClientChargeRepository;
import com.navi.net_pay_backend.domain.repository.PaymentAllocationRepository;
import com.navi.net_pay_backend.domain.repository.ReceiptRepository;
import com.navi.net_pay_backend.domain.service.AuditLogService;
import com.navi.net_pay_backend.domain.service.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppPaymentUseCase {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final AppPaymentRepository paymentRepository;
    private final AppClientRepository clientRepository;
    private final AppUserRepository userRepository;
    private final PaymentAllocationRepository allocationRepository;
    private final ClientChargeRepository clientChargeRepository;
    private final ReceiptRepository receiptRepository;
    private final ClientChargeGenerator chargeGenerator;
    private final ReceiptUseCase receiptUseCase;
    private final AuditLogService auditLogService;
    private final SecurityContext securityContext;

    public AppPayment findByHashId(String hashId) {
        AppPayment payment = paymentRepository.findByHashIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("payment_not_found"));
        return withAllocations(payment);
    }

    public PaginatedResult<AppPayment> findAll(AppPaymentQueryDto queryDto) {
        queryDto.setExcludeStatus(TypologyEnum.DELETED.getId());
        return paymentRepository.findAll(queryDto);
    }

    @Transactional
    public AppPayment create(AppPayment payment) {
        return createPayment(payment, true);
    }

    @Transactional
    public AppPayment createForImport(AppPayment payment, boolean dryRun) {
        return createPayment(payment, !dryRun);
    }

    private AppPayment createPayment(AppPayment payment, boolean storeReceiptDocument) {
        AppClient client = resolveClient(payment);
        validatePayment(payment);
        validateAttachmentUrl(payment.getAttachmentUrl());
        AppUser collector = resolveCollector(payment.getCollector());

        AppPayment saved = paymentRepository.save(buildNewPayment(payment, client, collector));
        applyLedger(client, saved);
        issueReceiptIfCompleted(saved, storeReceiptDocument);
        auditLogService.record("payment_created", "Registró pago", "PAYMENT", saved.getHashId(), after(saved));
        return withAllocations(saved);
    }

    @Transactional
    public AppPayment createAsCollector(AppPayment payment) {
        AppClient client = resolveClient(payment);
        validatePayment(payment);
        validateAttachmentUrl(payment.getAttachmentUrl());
        AppUser collector = currentCollector();

        AppPayment saved = paymentRepository.save(buildNewPayment(payment, client, collector));
        applyLedger(client, saved);
        issueReceiptIfCompleted(saved);
        auditLogService.record("payment_created", "Registró pago", "PAYMENT", saved.getHashId(), after(saved));
        return withAllocations(saved);
    }

    @Transactional
    public AppPayment update(String hashId, AppPayment payment) {
        AppPayment existing = findByHashId(hashId);

        if (isCompleted(existing) && receiptRepository.findByPaymentId(existing.getId()).isPresent()) {
            throw new DomainException("payment_locked_by_receipt");
        }

        AppClient client = payment.getClient() != null && payment.getClient().getId() != null
                ? resolveClient(payment)
                : existing.getClient();
        AppUser collector = payment.getCollector() != null && payment.getCollector().getId() != null
                ? resolveCollector(payment.getCollector())
                : null;
        AdmTypology method = payment.getTpPaymentMethod() != null
                ? payment.getTpPaymentMethod()
                : existing.getTpPaymentMethod();
        AdmTypology status = payment.getTpPaymentStatus() != null
                ? resolveStatus(payment.getTpPaymentStatus())
                : existing.getTpPaymentStatus();
        BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : existing.getAmount();
        LocalDate paidAt = payment.getPaidAt() != null ? payment.getPaidAt() : existing.getPaidAt();

        validatePayment(AppPayment.builder()
                .amount(amount)
                .paidAt(paidAt)
                .tpPaymentMethod(method)
                .build());
        String attachmentUrl = payment.getAttachmentUrl() != null
                ? trimToNull(payment.getAttachmentUrl())
                : existing.getAttachmentUrl();
        validateAttachmentUrl(attachmentUrl);

        allocationRepository.deleteByPaymentId(existing.getId());

        AppPayment updated = paymentRepository.save(AppPayment.builder()
                .id(existing.getId())
                .hashId(existing.getHashId())
                .importKey(existing.getImportKey())
                .client(client)
                .service(client.getService())
                .amount(amount)
                .paidAt(paidAt)
                .collector(collector)
                .tpPaymentMethod(method)
                .tpPaymentStatus(status)
                .reference(trimToNull(payment.getReference()))
                .attachmentUrl(attachmentUrl)
                .comment(trimToNull(payment.getComment()))
                .createdAt(existing.getCreatedAt())
                .build()
        );

        applyLedger(client, updated);
        issueReceiptIfCompleted(updated);
        auditLogService.record("payment_updated", "Modificó pago", "PAYMENT", updated.getHashId(),
                diff(existing, updated));
        return withAllocations(updated);
    }

    @Transactional
    public void delete(String hashId) {
        AppPayment existing = findByHashId(hashId);

        allocationRepository.deleteByPaymentId(existing.getId());

        paymentRepository.save(AppPayment.builder()
                .id(existing.getId())
                .hashId(existing.getHashId())
                .importKey(existing.getImportKey())
                .client(existing.getClient())
                .service(existing.getService())
                .amount(existing.getAmount())
                .paidAt(existing.getPaidAt())
                .collector(existing.getCollector())
                .tpPaymentMethod(existing.getTpPaymentMethod())
                .tpPaymentStatus(AdmTypology.builder().internalId(TypologyEnum.DELETED.getId()).build())
                .reference(existing.getReference())
                .attachmentUrl(existing.getAttachmentUrl())
                .comment(existing.getComment())
                .createdAt(existing.getCreatedAt())
                .deletedAt(LocalDateTime.now())
                .build()
        );

        auditLogService.record("payment_deleted", "Anuló pago", "PAYMENT", existing.getHashId(), before(existing));
    }

    // ── Receipt ─────────────────────────────────────────────────────────────

    private boolean isCompleted(AppPayment payment) {
        return payment.getTpPaymentStatus() != null
                && TypologyEnum.COMPLETED.getId().equals(payment.getTpPaymentStatus().getInternalId());
    }

    private void issueReceiptIfCompleted(AppPayment payment) {
        issueReceiptIfCompleted(payment, true);
    }

    private void issueReceiptIfCompleted(AppPayment payment, boolean storeDocument) {
        if (!isCompleted(payment) || payment.getClient() == null) {
            return;
        }
        ClientCharge firstCharge = clientChargeRepository
                .findByClientIdOrderByPeriod(payment.getClient().getId())
                .stream()
                .filter(charge -> allocationRepository.sumAmountByChargeId(charge.getId()).signum() > 0)
                .findFirst()
                .orElse(null);
        receiptUseCase.issueForPayment(payment, firstCharge, storeDocument);
    }

    // ── Ledger ──────────────────────────────────────────────────────────────

    private void applyLedger(AppClient client, AppPayment payment) {
        if (!isCompleted(payment) || payment.getAmount() == null || payment.getAmount().signum() <= 0) {
            return;
        }

        LocalDate paidAt = payment.getPaidAt() != null ? payment.getPaidAt() : LocalDate.now();
        List<ClientCharge> charges = chargeGenerator.ensureUpTo(client, paidAt);

        BigDecimal remaining = payment.getAmount();
        for (ClientCharge charge : charges) {
            if (remaining.signum() <= 0) {
                break;
            }
            remaining = allocate(payment, charge, remaining);
        }
        // Si sobra (remaining > 0) queda como saldo a favor del cliente; no se generan meses futuros.
    }

    private BigDecimal allocate(AppPayment payment, ClientCharge charge, BigDecimal remaining) {
        BigDecimal allocated = allocationRepository.sumAmountByChargeId(charge.getId());
        BigDecimal due = charge.getExpectedAmount().subtract(allocated);
        if (due.signum() <= 0) {
            return remaining;
        }
        BigDecimal apply = remaining.min(due);
        allocationRepository.save(PaymentAllocation.builder()
                .payment(AppPayment.builder().id(payment.getId()).build())
                .charge(charge)
                .amount(apply)
                .build());
        return remaining.subtract(apply);
    }

    private AppPayment withAllocations(AppPayment payment) {
        return payment.toBuilder()
                .allocations(allocationRepository.findByPaymentId(payment.getId()))
                .build();
    }

    // ── Build / resolve ─────────────────────────────────────────────────────

    private AppPayment buildNewPayment(AppPayment payment, AppClient client, AppUser collector) {
        return AppPayment.builder()
                .hashId(UUID.randomUUID().toString())
                .importKey(payment.getImportKey())
                .client(client)
                .service(client.getService())
                .amount(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .collector(collector)
                .tpPaymentMethod(payment.getTpPaymentMethod())
                .tpPaymentStatus(resolveStatus(payment.getTpPaymentStatus()))
                .reference(trimToNull(payment.getReference()))
                .attachmentUrl(trimToNull(payment.getAttachmentUrl()))
                .comment(trimToNull(payment.getComment()))
                .build();
    }

    private AppClient resolveClient(AppPayment payment) {
        if (payment.getClient() == null || payment.getClient().getId() == null) {
            throw new DomainException("payment_client_required");
        }
        return clientRepository.findByIdAndStatusNot(payment.getClient().getId(), TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("payment_client_not_found"));
    }

    private AppUser resolveCollector(AppUser collector) {
        if (collector == null) {
            return null;
        }
        if (collector.getId() == null) {
            throw new DomainException("payment_collector_required");
        }
        return userRepository.findByIdAndStatusNot(collector.getId(), TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("payment_collector_not_found"));
    }

    private AppUser currentCollector() {
        Long userId = securityContext.getUserId()
                .orElseThrow(() -> new DomainException("unauthorized"));
        return userRepository.findByIdAndStatusNot(userId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("user_not_found"));
    }

    private void validatePayment(AppPayment payment) {
        if (payment.getAmount() == null) {
            throw new DomainException("payment_amount_required");
        }
        if (payment.getAmount().compareTo(ZERO) <= 0) {
            throw new DomainException("payment_amount_invalid");
        }
        if (payment.getPaidAt() == null) {
            throw new DomainException("payment_paid_at_required");
        }
        if (!hasTypologyKey(payment.getTpPaymentMethod())) {
            throw new DomainException("payment_method_required");
        }
    }

    private AdmTypology resolveStatus(AdmTypology status) {
        Long resolved = status != null ? status.getInternalId() : null;
        if (resolved == null && status != null) {
            resolved = status.getTypologyId();
        }
        if (resolved == null) {
            resolved = TypologyEnum.PENDING.getId();
        }
        if (TypologyEnum.DELETED.getId().equals(resolved)) {
            throw new DomainException("payment_cannot_be_deleted_via_update");
        }
        return AdmTypology.builder().internalId(resolved).build();
    }

    private boolean hasTypologyKey(AdmTypology typology) {
        return typology != null
                && (typology.getInternalId() != null || typology.getTypologyId() != null);
    }

    private Map<String, Object> snapshot(AppPayment payment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("amount", payment.getAmount());
        map.put("paidAt", payment.getPaidAt() != null ? payment.getPaidAt().toString() : null);
        map.put("reference", payment.getReference());
        map.put("attachmentUrl", payment.getAttachmentUrl());
        map.put("comment", payment.getComment());
        map.put("status", payment.getTpPaymentStatus() != null ? payment.getTpPaymentStatus().getInternalId() : null);
        map.put("method", payment.getTpPaymentMethod() != null ? payment.getTpPaymentMethod().getInternalId() : null);
        return map;
    }

    private Map<String, Object> before(AppPayment payment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("before", snapshot(payment));
        return map;
    }

    private Map<String, Object> after(AppPayment payment) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("after", snapshot(payment));
        return map;
    }

    private Map<String, Object> diff(AppPayment before, AppPayment after) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("before", snapshot(before));
        map.put("after", snapshot(after));
        return map;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateAttachmentUrl(String attachmentUrl) {
        if (attachmentUrl != null && (attachmentUrl.length() > 2048
                || !attachmentUrl.startsWith("https://res.cloudinary.com/"))) {
            throw new DomainException("payment_attachment_url_invalid");
        }
    }
}
