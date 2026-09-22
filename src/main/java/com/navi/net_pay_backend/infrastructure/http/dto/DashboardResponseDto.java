package com.navi.net_pay_backend.infrastructure.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponseDto {
    private DashboardSummaryDto summary;
    private List<AppPaymentResponseDto> recentPayments;
    private List<AuditLogResponseDto> recentActivities;
    private List<PlanClientCountDto> clientsByPlan;
    private List<MethodCollectionDto> collectionsByMethod;
    private List<MonthlyMethodCollectionDto> collectionsByMethodByMonth;
}
