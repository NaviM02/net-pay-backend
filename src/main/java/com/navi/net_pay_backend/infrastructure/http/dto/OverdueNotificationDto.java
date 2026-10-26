package com.navi.net_pay_backend.infrastructure.http.dto;

import java.math.BigDecimal;
import java.util.List;

public record OverdueNotificationDto(
        String id,
        String type,
        String title,
        String message,
        String clientHashId,
        String clientName,
        long overdueMonths,
        BigDecimal totalDebt,
        List<String> overduePeriods
) {
}
