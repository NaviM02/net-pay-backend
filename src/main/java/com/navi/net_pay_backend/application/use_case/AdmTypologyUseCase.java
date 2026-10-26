package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.repository.AdmTypologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdmTypologyUseCase {

    private final AdmTypologyRepository typologyRepository;

    public AdmTypology findByInternalId(Long internalId) {
        return typologyRepository.findByInternalId(internalId).orElseThrow(() -> new DomainException("typology_not_found"));
    }

    public List<AdmTypology> findByParentInternalId(Long parentInternalId) {
        return typologyRepository.findByParentInternalId(parentInternalId);
    }
}