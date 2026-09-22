package com.navi.net_pay_backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AppReceiptQueryDto extends QueryDto {
    private String search;
    private Long periodId;
    private String clientHashId;
    private Long collectorId;
    private Long excludeStatus;
}
