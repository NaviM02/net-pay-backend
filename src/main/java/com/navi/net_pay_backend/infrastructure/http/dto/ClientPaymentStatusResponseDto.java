package com.navi.net_pay_backend.infrastructure.http.dto;

import java.math.BigDecimal;

public record ClientPaymentStatusResponseDto(
        String clientHashId,
        String status,
        BigDecimal totalExpected,
        BigDecimal totalPaid,
        BigDecimal balance,
        BigDecimal credit,
        int overdueCount
) {
}
