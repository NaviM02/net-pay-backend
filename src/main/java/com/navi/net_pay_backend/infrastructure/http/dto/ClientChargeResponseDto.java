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
public class ClientChargeResponseDto {
    private String hashId;
    private Integer year;
    private Integer month;
    private String periodLabel;
    private BigDecimal expectedAmount;
    private BigDecimal paidAmount;
    private BigDecimal balance;
    private String status;
    private boolean overdue;
}
