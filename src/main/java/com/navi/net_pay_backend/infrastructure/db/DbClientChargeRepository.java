package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.domain.entity.BillingPeriod;
import com.navi.net_pay_backend.domain.entity.ClientCharge;
import com.navi.net_pay_backend.domain.repository.ClientChargeRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.AppClientEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.AppServiceEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.BillingPeriodEntity;
import com.navi.net_pay_backend.infrastructure.db.entity.ClientChargeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DbClientChargeRepository implements ClientChargeRepository {

    private final ClientChargeJpaRepository repository;
    private final AppClientJpaRepository clientRepository;
    private final AppServiceJpaRepository serviceRepository;
    private final BillingPeriodJpaRepository periodRepository;

    @Override
    public Optional<ClientCharge> findByClientIdAndPeriodId(Long clientId, Long periodId) {
        return repository.findByClientIdAndPeriodId(clientId, periodId).map(this::toDomain);
    }

    @Override
    public Optional<ClientCharge> findByHashId(String hashId) {
        return repository.findByHashId(hashId).map(this::toDomain);
    }

    @Override
    public List<ClientCharge> findByClientIdOrderByPeriod(Long clientId) {
        return repository.findByClientIdOrderByPeriodYearAscPeriodMonthAsc(clientId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public ClientCharge save(ClientCharge charge) {
        ClientChargeEntity entity = charge.getId() != null
                ? repository.findById(charge.getId()).orElseGet(ClientChargeEntity::new)
                : new ClientChargeEntity();

        entity.setHashId(charge.getHashId());
        entity.setExpectedAmount(charge.getExpectedAmount());
        entity.setDeletedAt(charge.getDeletedAt());

        if (charge.getClient() != null) {
            entity.setClient(clientRepository.findById(charge.getClient().getId())
                    .orElseThrow(() -> new com.navi.net_pay_backend.domain.exception.EntityNotFoundException("charge_client_not_found")));
        }
        if (charge.getPeriod() != null) {
            entity.setPeriod(periodRepository.findById(charge.getPeriod().getId())
                    .orElseThrow(() -> new com.navi.net_pay_backend.domain.exception.EntityNotFoundException("charge_period_not_found")));
        }
        if (charge.getService() != null) {
            entity.setService(serviceRepository.findById(charge.getService().getId())
                    .orElseThrow(() -> new com.navi.net_pay_backend.domain.exception.EntityNotFoundException("charge_service_not_found")));
        }

        if (charge.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setUpdatedAt(LocalDateTime.now());

        return toDomain(repository.save(entity));
    }

    private ClientCharge toDomain(ClientChargeEntity entity) {
        if (entity == null) return null;
        return ClientCharge.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .expectedAmount(entity.getExpectedAmount())
                .client(toDomain(entity.getClient()))
                .period(toDomain(entity.getPeriod()))
                .service(toDomain(entity.getService()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deletedAt(entity.getDeletedAt())
                .build();
    }

    private AppClient toDomain(AppClientEntity entity) {
        if (entity == null) return null;
        return AppClient.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .customerSince(entity.getCustomerSince())
                .service(toDomain(entity.getService()))
                .build();
    }

    private AppService toDomain(AppServiceEntity entity) {
        if (entity == null) return null;
        return AppService.builder()
                .id(entity.getId())
                .hashId(entity.getHashId())
                .name(entity.getName())
                .monthlyPrice(entity.getMonthlyPrice())
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
                .closed(entity.getClosed())
                .build();
    }
}
