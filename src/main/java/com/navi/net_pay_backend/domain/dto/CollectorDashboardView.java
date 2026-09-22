package com.navi.net_pay_backend.domain.dto;

import com.navi.net_pay_backend.domain.entity.AppPayment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectorDashboardView {
    private CollectorDashboardSummary summary;
    private List<AppPayment> recentPayments;
    private List<OverdueClientView> overdueClients;
    private List<MethodCollection> collectionsByMethod;
    private List<MonthlyMethodCollection> collectionsByMethodByMonth;
}
