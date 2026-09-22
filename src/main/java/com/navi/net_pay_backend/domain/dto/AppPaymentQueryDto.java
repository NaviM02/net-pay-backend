package com.navi.net_pay_backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class AppPaymentQueryDto extends QueryDto {
    private String search;
    private Long status;
    private Long method;
    private LocalDate paidAtFrom;
    private LocalDate paidAtTo;
    private String clientHashId;
    private Long collectorId;
    private Long excludeStatus;
}
