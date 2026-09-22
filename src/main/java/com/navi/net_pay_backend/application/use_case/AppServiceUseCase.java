package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.AppServiceQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.exception.EntityAlreadyExistsException;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AppServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppServiceUseCase {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final AppServiceRepository serviceRepository;

    public AppService findByHashId(String hashId) {
        return serviceRepository.findByHashIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("service_not_found"));
    }

    public PaginatedResult<AppService> findAll(AppServiceQueryDto queryDto) {
        queryDto.setExcludeStatus(TypologyEnum.DELETED.getId());
        return serviceRepository.findAll(queryDto);
    }

    public AppService create(AppService request) {
        validateRequiredFields(request);
        validateNameIsAvailable(request.getName(), null);

        return serviceRepository.save(AppService.builder()
                .hashId(UUID.randomUUID().toString())
                .name(request.getName().trim())
                .speed(request.getSpeed().trim())
                .monthlyPrice(request.getMonthlyPrice())
                .clientCount(0)
                .tpType(request.getTpType())
                .tpStatus(request.getTpStatus())
                .build()
        );
    }

    public AppService update(String hashId, AppService request) {
        AppService existing = serviceRepository.findByHashIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("service_not_found"));

        validateRequiredFields(request);
        validateNameIsAvailable(request.getName().trim(), existing.getId());

        return serviceRepository.save(AppService.builder()
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
    }

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
}
