package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.dto.AppPaymentQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppPayment;

import java.math.BigDecimal;
import java.util.Optional;

public interface AppPaymentRepository {
    Optional<AppPayment> findByHashIdAndStatusNot(String hashId, Long statusId);

    PaginatedResult<AppPayment> findAll(AppPaymentQueryDto queryDto);

    BigDecimal sumAmountByClientId(Long clientId, Long excludeStatusId);

    AppPayment save(AppPayment payment);
}
