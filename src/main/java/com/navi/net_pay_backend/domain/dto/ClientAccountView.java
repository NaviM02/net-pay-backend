package com.navi.net_pay_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ClientAccountView {
    private String clientHashId;
    private BigDecimal totalExpected;
    private BigDecimal totalPaid;
    private BigDecimal balance;
    private BigDecimal credit;
    private int overdueCount;
    private long totalCharges;
    private List<ClientChargeView> charges;
}
