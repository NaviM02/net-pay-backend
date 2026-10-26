package com.navi.net_pay_backend.domain.dto;

import com.navi.net_pay_backend.domain.entity.AppPayment;
import com.navi.net_pay_backend.domain.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardView {
    private DashboardSummary summary;
    private List<AppPayment> recentPayments;
    private List<AuditLog> recentActivities;
    private List<PlanClientCount> clientsByPlan;
    private List<MethodCollection> collectionsByMethod;
    private List<MonthlyMethodCollection> collectionsByMethodByMonth;
}
