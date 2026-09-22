package com.navi.net_pay_backend.infrastructure.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardSummaryDto {
    private long activeClients;
    private BigDecimal monthCollection;
    private long overdueClients;
    private long receiptsIssued;
    private int year;
    private int month;
    private String monthLabel;
}
