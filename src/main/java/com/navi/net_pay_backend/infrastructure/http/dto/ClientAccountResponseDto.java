package com.navi.net_pay_backend.infrastructure.http.dto;

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
public class ClientAccountResponseDto {
    private String clientHashId;
    private BigDecimal totalExpected;
    private BigDecimal totalPaid;
    private BigDecimal balance;
    private BigDecimal credit;
    private int overdueCount;
    private List<ClientChargeResponseDto> charges;
}
