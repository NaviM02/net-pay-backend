package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.dto.AppClientQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppClient;

import java.util.Optional;

public interface AppClientRepository {
    Optional<AppClient> findByHashIdAndStatusNot(String hashId, Long statusId);

    PaginatedResult<AppClient> findAll(AppClientQueryDto queryDto);

    long countByServiceIdAndStatusNot(Long serviceId, Long statusId);

    AppClient save(AppClient client);
}
