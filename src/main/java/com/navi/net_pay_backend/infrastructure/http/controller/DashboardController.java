package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.DashboardUseCase;
import com.navi.net_pay_backend.domain.dto.CollectorDashboardView;
import com.navi.net_pay_backend.domain.dto.DashboardView;
import com.navi.net_pay_backend.infrastructure.http.dto.CollectorDashboardResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.DashboardResponseDto;
import com.navi.net_pay_backend.infrastructure.http.mapper.HttpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardUseCase dashboardUseCase;
    private final HttpMapper httpMapper;

    @GetMapping
    public ResponseEntity<DashboardResponseDto> dashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "5") int recentLimit,
            @RequestParam(defaultValue = "6") int months
    ) {
        DashboardView view = dashboardUseCase.dashboard(year, month, recentLimit, months);
        return ResponseEntity.ok(httpMapper.toDashboardResponse(view));
    }

    @GetMapping("/collector")
    public ResponseEntity<CollectorDashboardResponseDto> collectorDashboard(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "5") int recentLimit,
            @RequestParam(defaultValue = "6") int months
    ) {
        CollectorDashboardView view = dashboardUseCase.collectorDashboard(year, month, recentLimit, months);
        return ResponseEntity.ok(httpMapper.toCollectorDashboardResponse(view));
    }
}
