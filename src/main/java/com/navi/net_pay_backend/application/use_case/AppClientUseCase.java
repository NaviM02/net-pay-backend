package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.AppClientQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.exception.EntityAlreadyExistsException;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AppClientRepository;
import com.navi.net_pay_backend.domain.repository.AppServiceRepository;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppClientUseCase {

    private final AppClientRepository clientRepository;
    private final AppServiceRepository serviceRepository;
    private final AppUserRepository userRepository;
    private final AuditLogService auditLogService;

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

    @Transactional
    public AppClient create(AppClient client) {
        validateRequiredFields(client);
        validateBusinessNameAvailable(client.getBusinessName(), null);
        AppUser collector = resolveCollector(client.getCollector());

        AppClient saved = clientRepository.save(AppClient.builder()
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
                .collector(collector)
                .tpStatus(resolveStatus(client.getTpStatus()))
                .build()
        );
        auditLogService.record("client_created", "Creó cliente", "CLIENT", saved.getHashId(), after(saved));
        return saved;
    }

    @Transactional
    public AppClient update(String hashId, AppClient client) {
        AppClient existing = findByHashId(hashId);

        validateRequiredFields(client);
        validateBusinessNameAvailable(client.getBusinessName(), existing.getId());
        AppUser collector = resolveCollector(client.getCollector());

        AppClient saved = clientRepository.save(AppClient.builder()
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
                .collector(collector)
                .tpStatus(existing.getTpStatus())
                .createdAt(existing.getCreatedAt())
                .build()
        );
        auditLogService.record("client_updated", "Modificó cliente", "CLIENT", saved.getHashId(),
                diff(existing, saved));
        return saved;
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

    public AppClient assignCollectorByHashId(String hashId, String collectorHashId) {
        AppClient existing = findByHashId(hashId);
        AppUser collector = collectorHashId != null && !collectorHashId.isBlank()
                ? userRepository.findByPublicIdAndStatusNot(collectorHashId, TypologyEnum.DELETED.getId())
                        .orElseThrow(() -> new EntityNotFoundException("client_collector_not_found"))
                : null;

        return clientRepository.save(existing.toBuilder().collector(collector).build());
    }

    private AppUser resolveCollector(AppUser collector) {
        if (collector == null) {
            return null;
        }
        if (collector.getHashId() != null && !collector.getHashId().isBlank()) {
            return userRepository.findByPublicIdAndStatusNot(collector.getHashId(), TypologyEnum.DELETED.getId())
                    .orElseThrow(() -> new EntityNotFoundException("client_collector_not_found"));
        }
        if (collector.getId() != null) {
            return userRepository.findByIdAndStatusNot(collector.getId(), TypologyEnum.DELETED.getId())
                    .orElseThrow(() -> new EntityNotFoundException("client_collector_not_found"));
        }
        throw new EntityNotFoundException("client_collector_not_found");
    }

    @Transactional
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
        auditLogService.record("client_deleted", "Eliminó cliente", "CLIENT", existing.getHashId(), before(existing));
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

    private void validateBusinessNameAvailable(String businessName, Long currentId) {
        String normalizedName = businessName.trim();
        boolean exists = currentId == null
                ? clientRepository.existsByBusinessNameAndStatusNot(normalizedName, TypologyEnum.DELETED.getId())
                : clientRepository.existsByBusinessNameAndStatusNotAndIdNot(normalizedName, TypologyEnum.DELETED.getId(), currentId);
        if (exists) {
            throw new EntityAlreadyExistsException("client_business_name_already_exists");
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

    private Map<String, Object> snapshot(AppClient client) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("businessName", client.getBusinessName());
        map.put("contactName", client.getContactName());
        map.put("contactEmail", client.getContactEmail());
        map.put("contactPhone", client.getContactPhone());
        map.put("address", client.getAddress());
        map.put("customerSince", client.getCustomerSince() != null ? client.getCustomerSince().toString() : null);
        map.put("status", client.getTpStatus() != null ? client.getTpStatus().getInternalId() : null);
        map.put("serviceHashId", client.getService() != null ? client.getService().getHashId() : null);
        map.put("collectorHashId", client.getCollector() != null ? client.getCollector().getHashId() : null);
        return map;
    }

    private Map<String, Object> before(AppClient client) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("before", snapshot(client));
        return map;
    }

    private Map<String, Object> after(AppClient client) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("after", snapshot(client));
        return map;
    }

    private Map<String, Object> diff(AppClient before, AppClient after) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("before", snapshot(before));
        map.put("after", snapshot(after));
        return map;
    }
}
