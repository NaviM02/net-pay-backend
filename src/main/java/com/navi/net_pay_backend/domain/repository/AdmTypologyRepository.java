package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.entity.AdmTypology;

import java.util.List;
import java.util.Optional;

public interface AdmTypologyRepository {

    Optional<AdmTypology> findByInternalId(Long internalId);

    List<AdmTypology> findByParentInternalId(Long parentInternalId);
}