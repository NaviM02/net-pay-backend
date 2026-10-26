package com.navi.net_pay_backend.domain.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter @Setter
public class AppPaymentQueryDto extends QueryDto {
    private String search;
    private Long status;
    private Long method;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;
    private String clientHashId;
    private Long collectorId;
    private Long excludeStatus;
}
