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
public class CollectorDashboardResponseDto {
    private CollectorDashboardSummaryDto summary;
    private List<AppPaymentResponseDto> recentPayments;
    private List<OverdueClientResponseDto> overdueClients;
    private List<MethodCollectionDto> collectionsByMethod;
    private List<MonthlyMethodCollectionDto> collectionsByMethodByMonth;
}
