package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.dto.AppClientQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppClient;

import java.util.List;
import java.util.Optional;

public interface AppClientRepository {
    Optional<AppClient> findByIdAndStatusNot(Long id, Long statusId);

    Optional<AppClient> findByHashIdAndStatusNot(String hashId, Long statusId);

    List<AppClient> findAllNotDeleted();

    List<AppClient> findByCollectorId(Long collectorId);

    PaginatedResult<AppClient> findAll(AppClientQueryDto queryDto);

    long countByServiceIdAndStatusNot(Long serviceId, Long statusId);

    boolean existsByBusinessNameAndStatusNot(String businessName, Long statusId);

    boolean existsByBusinessNameAndStatusNotAndIdNot(String businessName, Long statusId, Long id);

    AppClient save(AppClient client);
}
