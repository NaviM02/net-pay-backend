package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.repository.AdmTypologyRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.AdmTypologyEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DbAdmTypologyRepository implements AdmTypologyRepository {

    private final AdmTypologyJpaRepository repository;

    @Override
    public Optional<AdmTypology> findByInternalId(Long internalId) {
        return repository.findByInternalId(internalId).map(this::toDomain);
    }

    @Override
    public List<AdmTypology> findByParentInternalId(Long parentInternalId) {
        return repository.findByParentInternalId(parentInternalId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private AdmTypology toDomain(AdmTypologyEntity entity) {
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