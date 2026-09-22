package com.navi.net_pay_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OverdueClientView {
    private String clientHashId;
    private String businessName;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String serviceHashId;
    private String planName;
    private Long statusId;
    private String statusCode;
    private String statusDescription;
    private long overdueMonths;
    private BigDecimal totalDebt;
    private List<String> overduePeriods;
}
