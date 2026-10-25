package com.navi.net_pay_backend.infrastructure.http.dto;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationsResponseDto(
        LocalDateTime generatedAt,
        int count,
        List<OverdueNotificationDto> notifications
) {
}
