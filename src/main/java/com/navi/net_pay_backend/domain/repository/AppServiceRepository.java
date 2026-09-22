package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.dto.AppServiceQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppService;

import java.util.Optional;

public interface AppServiceRepository {
    Optional<AppService> findByHashIdAndStatusNot(String hashId, Long statusId);

    PaginatedResult<AppService> findAll(AppServiceQueryDto queryDto);

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndDeletedAtIsNullAndIdNot(String name, Long id);

    AppService save(AppService service);
}
