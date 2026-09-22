package com.navi.net_pay_backend.domain.dto;

import com.navi.net_pay_backend.domain.enums.ChargeStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class ClientChargeView {
    private String hashId;
    private Integer year;
    private Integer month;
    private String periodLabel;
    private BigDecimal expectedAmount;
    private BigDecimal paidAmount;
    private BigDecimal balance;
    private ChargeStatusEnum status;
    private boolean overdue;
}
