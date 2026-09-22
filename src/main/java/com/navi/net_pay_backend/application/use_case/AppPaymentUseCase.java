package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.AppPaymentQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.AppPayment;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AppClientRepository;
import com.navi.net_pay_backend.domain.repository.AppPaymentRepository;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.service.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppPaymentUseCase {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final AppPaymentRepository paymentRepository;
    private final AppClientRepository clientRepository;
    private final AppUserRepository userRepository;
    private final SecurityContext securityContext;

    public AppPayment findByHashId(String hashId) {
        return paymentRepository.findByHashIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("payment_not_found"));
    }

    public PaginatedResult<AppPayment> findAll(AppPaymentQueryDto queryDto) {
        queryDto.setExcludeStatus(TypologyEnum.DELETED.getId());
        return paymentRepository.findAll(queryDto);
    }

    public AppPayment create(AppPayment payment) {
        AppClient client = resolveClient(payment);
        validatePayment(payment);
        AppUser collector = resolveCollector(payment.getCollector());

        return paymentRepository.save(buildNewPayment(payment, client, collector));
    }

    public AppPayment createAsCollector(AppPayment payment) {
        AppClient client = resolveClient(payment);
        validatePayment(payment);
        AppUser collector = currentCollector();

        return paymentRepository.save(buildNewPayment(payment, client, collector));
    }

    public AppPayment update(String hashId, AppPayment payment) {
        AppPayment existing = findByHashId(hashId);
        validatePayment(payment);

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

        return paymentRepository.save(AppPayment.builder()
                .id(existing.getId())
                .hashId(existing.getHashId())
                .client(client)
                .service(client.getService())
                .amount(amount)
                .paidAt(paidAt)
                .collector(collector)
                .tpPaymentMethod(method)
                .tpPaymentStatus(status)
                .comment(trimToNull(payment.getComment()))
                .createdAt(existing.getCreatedAt())
                .build()
        );
    }

    public void delete(String hashId) {
        AppPayment existing = findByHashId(hashId);

        paymentRepository.save(AppPayment.builder()
                .id(existing.getId())
                .hashId(existing.getHashId())
                .client(existing.getClient())
                .service(existing.getService())
                .amount(existing.getAmount())
                .paidAt(existing.getPaidAt())
                .collector(existing.getCollector())
                .tpPaymentMethod(existing.getTpPaymentMethod())
                .tpPaymentStatus(AdmTypology.builder().internalId(TypologyEnum.DELETED.getId()).build())
                .comment(existing.getComment())
                .createdAt(existing.getCreatedAt())
                .deletedAt(LocalDateTime.now())
                .build()
        );
    }

    private AppPayment buildNewPayment(AppPayment payment, AppClient client, AppUser collector) {
        return AppPayment.builder()
                .hashId(UUID.randomUUID().toString())
                .client(client)
                .service(client.getService())
                .amount(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .collector(collector)
                .tpPaymentMethod(payment.getTpPaymentMethod())
                .tpPaymentStatus(resolveStatus(payment.getTpPaymentStatus()))
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
        if (payment.getAmount().compareTo(ZERO) < 0) {
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

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
