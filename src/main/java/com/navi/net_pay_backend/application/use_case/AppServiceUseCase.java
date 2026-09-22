package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.AppServiceQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.exception.EntityAlreadyExistsException;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AppServiceRepository;
import com.navi.net_pay_backend.domain.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppServiceUseCase {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final AppServiceRepository serviceRepository;
    private final AuditLogService auditLogService;

    public AppService findByHashId(String hashId) {
        return serviceRepository.findByHashIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("service_not_found"));
    }

    public PaginatedResult<AppService> findAll(AppServiceQueryDto queryDto) {
        queryDto.setExcludeStatus(TypologyEnum.DELETED.getId());
        return serviceRepository.findAll(queryDto);
    }

    @Transactional
    public AppService create(AppService request) {
        validateRequiredFields(request);
        validateNameIsAvailable(request.getName(), null);

        AppService saved = serviceRepository.save(AppService.builder()
                .hashId(UUID.randomUUID().toString())
                .name(request.getName().trim())
                .speed(request.getSpeed().trim())
                .monthlyPrice(request.getMonthlyPrice())
                .clientCount(0)
                .tpType(request.getTpType())
                .tpStatus(request.getTpStatus())
                .build()
        );
        auditLogService.record("service_created", "Creó servicio", "SERVICE", saved.getHashId(), after(saved));
        return saved;
    }

    @Transactional
    public AppService update(String hashId, AppService request) {
        AppService existing = serviceRepository.findByHashIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("service_not_found"));

        validateRequiredFields(request);
        validateNameIsAvailable(request.getName().trim(), existing.getId());

        AppService saved = serviceRepository.save(AppService.builder()
                .id(existing.getId())
                .hashId(existing.getHashId())
                .name(request.getName().trim())
                .speed(request.getSpeed().trim())
                .monthlyPrice(request.getMonthlyPrice())
                .clientCount(existing.getClientCount())
                .tpType(request.getTpType())
                .tpStatus(request.getTpStatus())
                .createdAt(existing.getCreatedAt())
                .build()
        );
        auditLogService.record("service_updated", "Modificó servicio", "SERVICE", saved.getHashId(),
                diff(existing, saved));
        return saved;
    }

    @Transactional
    public void delete(String hashId) {
        AppService existing = serviceRepository.findByHashIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("service_not_found"));

        serviceRepository.save(AppService.builder()
                .id(existing.getId())
                .hashId(existing.getHashId())
                .name(existing.getName())
                .speed(existing.getSpeed())
                .monthlyPrice(existing.getMonthlyPrice())
                .clientCount(existing.getClientCount())
                .tpType(existing.getTpType())
                .tpStatus(existing.getTpStatus())
                .createdAt(existing.getCreatedAt())
                .deletedAt(LocalDateTime.now())
                .build()
        );
        auditLogService.record("service_deleted", "Eliminó servicio", "SERVICE", existing.getHashId(), before(existing));
    }

    private void validateRequiredFields(AppService request) {
        if (isBlank(request.getName())) {
            throw new DomainException("service_name_required");
        }
        if (isBlank(request.getSpeed())) {
            throw new DomainException("service_speed_required");
        }
        if (request.getMonthlyPrice() == null) {
            throw new DomainException("service_price_required");
        }
        if (request.getMonthlyPrice().compareTo(ZERO) < 0) {
            throw new DomainException("service_price_invalid");
        }
    }

    private void validateNameIsAvailable(String name, Long currentId) {
        boolean taken = currentId == null
                ? serviceRepository.existsByNameAndDeletedAtIsNull(name)
                : serviceRepository.existsByNameAndDeletedAtIsNullAndIdNot(name, currentId);

        if (taken) {
            throw new EntityAlreadyExistsException("service_name_must_be_unique");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Map<String, Object> snapshot(AppService service) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", service.getName());
        map.put("speed", service.getSpeed());
        map.put("monthlyPrice", service.getMonthlyPrice());
        map.put("type", service.getTpType() != null ? service.getTpType().getInternalId() : null);
        map.put("status", service.getTpStatus() != null ? service.getTpStatus().getInternalId() : null);
        return map;
    }

    private Map<String, Object> before(AppService service) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("before", snapshot(service));
        return map;
    }

    private Map<String, Object> after(AppService service) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("after", snapshot(service));
        return map;
    }

    private Map<String, Object> diff(AppService before, AppService after) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("before", snapshot(before));
        map.put("after", snapshot(after));
        return map;
    }
}
