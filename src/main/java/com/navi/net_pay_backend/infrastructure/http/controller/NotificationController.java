package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.OverdueNotificationUseCase;
import com.navi.net_pay_backend.domain.dto.OverdueClientView;
import com.navi.net_pay_backend.infrastructure.http.dto.NotificationsResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.OverdueNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final OverdueNotificationUseCase notificationUseCase;

    @GetMapping
    public ResponseEntity<NotificationsResponseDto> findNotifications(Authentication authentication) {
        boolean includeAllClients = authentication.getAuthorities().stream()
                .anyMatch(authority -> "dashboard:read".equals(authority.getAuthority()));
        List<OverdueNotificationDto> notifications = notificationUseCase
                .findOverdueClients(includeAllClients).stream()
                .map(this::toNotification)
                .toList();

        return ResponseEntity.ok(new NotificationsResponseDto(LocalDateTime.now(), notifications.size(), notifications));
    }

    private OverdueNotificationDto toNotification(OverdueClientView client) {
        long months = client.getOverdueMonths();
        String clientName = client.getBusinessName();
        String message = clientName + " tiene " + months + " "
                + (months == 1 ? "mensualidad vencida" : "mensualidades vencidas") + ".";
        return new OverdueNotificationDto(
                "overdue-client-" + client.getClientHashId(),
                "CLIENT_PAYMENT_OVERDUE",
                "Cliente con pagos atrasados",
                message,
                client.getClientHashId(),
                clientName,
                months,
                client.getTotalDebt(),
                client.getOverduePeriods());
    }
}
