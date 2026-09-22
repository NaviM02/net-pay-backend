package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.dto.AppReceiptQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.Receipt;

import java.util.Optional;

public interface ReceiptRepository {
    Optional<Receipt> findByHashId(String hashId);

    Optional<Receipt> findByPaymentId(Long paymentId);

    PaginatedResult<Receipt> findAll(AppReceiptQueryDto queryDto);

    Receipt save(Receipt receipt);
}
