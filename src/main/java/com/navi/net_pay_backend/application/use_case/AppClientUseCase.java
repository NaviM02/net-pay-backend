package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.AppClientQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AppClientRepository;
import com.navi.net_pay_backend.domain.repository.AppServiceRepository;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppClientUseCase {

    private final AppClientRepository clientRepository;
    private final AppServiceRepository serviceRepository;
    private final AppUserRepository userRepository;

    public AppClient findByHashId(String hashId) {
        return clientRepository.findByHashIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("client_not_found"));
    }

    public PaginatedResult<AppClient> findAll(AppClientQueryDto queryDto) {
        queryDto.setExcludeStatus(TypologyEnum.DELETED.getId());
        return clientRepository.findAll(queryDto);
    }

    public PaginatedResult<AppClient> findByCollectorHashId(String collectorHashId, AppClientQueryDto queryDto) {
        AppUser collector = userRepository.findByPublicIdAndStatusNot(collectorHashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("user_not_found"));

        queryDto.setCollectorId(collector.getId());
        queryDto.setExcludeStatus(TypologyEnum.DELETED.getId());
        return clientRepository.findAll(queryDto);
    }

    public AppClient create(AppClient client) {
        validateRequiredFields(client);

        return clientRepository.save(AppClient.builder()
                .hashId(UUID.randomUUID().toString())
                .businessName(client.getBusinessName().trim())
                .contactName(client.getContactName().trim())
                .contactEmail(trimToNull(client.getContactEmail()))
                .contactPhone(trimToNull(client.getContactPhone()))
                .address(trimToNull(client.getAddress()))
                .latitude(client.getLatitude())
                .longitude(client.getLongitude())
                .customerSince(client.getCustomerSince() != null ? client.getCustomerSince() : LocalDate.now())
                .service(client.getService())
                .collector(client.getCollector())
                .tpStatus(resolveStatus(client.getTpStatus()))
                .build()
        );
    }

    public AppClient update(String hashId, AppClient client) {
        AppClient existing = findByHashId(hashId);

        validateRequiredFields(client);

        return clientRepository.save(AppClient.builder()
                .id(existing.getId())
                .hashId(existing.getHashId())
                .businessName(client.getBusinessName().trim())
                .contactName(client.getContactName().trim())
                .contactEmail(trimToNull(client.getContactEmail()))
                .contactPhone(trimToNull(client.getContactPhone()))
                .address(trimToNull(client.getAddress()))
                .latitude(client.getLatitude())
                .longitude(client.getLongitude())
                .customerSince(client.getCustomerSince() != null ? client.getCustomerSince() : existing.getCustomerSince())
                .service(client.getService())
                .collector(client.getCollector())
                .tpStatus(existing.getTpStatus())
                .createdAt(existing.getCreatedAt())
                .build()
        );
    }

    public AppClient assignCollector(String hashId, Long collectorId) {
        AppClient existing = findByHashId(hashId);
        AppUser collector = collectorId != null
                ? userRepository.findByIdAndStatusNot(collectorId, TypologyEnum.DELETED.getId())
                        .orElseThrow(() -> new EntityNotFoundException("client_collector_not_found"))
                : null;

        return clientRepository.save(AppClient.builder()
                .id(existing.getId())
                .hashId(existing.getHashId())
                .businessName(existing.getBusinessName())
                .contactName(existing.getContactName())
                .contactEmail(existing.getContactEmail())
                .contactPhone(existing.getContactPhone())
                .address(existing.getAddress())
                .latitude(existing.getLatitude())
                .longitude(existing.getLongitude())
                .customerSince(existing.getCustomerSince())
                .service(existing.getService())
                .collector(collector)
                .tpStatus(existing.getTpStatus())
                .createdAt(existing.getCreatedAt())
                .build()
        );
    }

    public void delete(String hashId) {
        AppClient existing = findByHashId(hashId);

        clientRepository.save(AppClient.builder()
                .id(existing.getId())
                .hashId(existing.getHashId())
                .businessName(existing.getBusinessName())
                .contactName(existing.getContactName())
                .contactEmail(existing.getContactEmail())
                .contactPhone(existing.getContactPhone())
                .address(existing.getAddress())
                .latitude(existing.getLatitude())
                .longitude(existing.getLongitude())
                .customerSince(existing.getCustomerSince())
                .service(existing.getService())
                .collector(existing.getCollector())
                .tpStatus(AdmTypology.builder().internalId(TypologyEnum.DELETED.getId()).build())
                .createdAt(existing.getCreatedAt())
                .deletedAt(LocalDateTime.now())
                .build()
        );
    }

    private AdmTypology resolveStatus(AdmTypology status) {
        Long resolved = status != null ? status.getInternalId() : null;
        if (resolved == null && status != null) {
            resolved = status.getTypologyId();
        }
        if (resolved == null) {
            resolved = TypologyEnum.ACTIVE.getId();
        }
        if (TypologyEnum.DELETED.getId().equals(resolved)) {
            throw new DomainException("client_cannot_be_deleted_via_update");
        }
        return AdmTypology.builder().internalId(resolved).build();
    }

    private void validateRequiredFields(AppClient client) {
        if (isBlank(client.getBusinessName())) {
            throw new DomainException("client_business_name_required");
        }
        if (isBlank(client.getContactName())) {
            throw new DomainException("client_contact_name_required");
        }
        if (client.getService() == null || client.getService().getId() == null) {
            throw new DomainException("client_service_required");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
