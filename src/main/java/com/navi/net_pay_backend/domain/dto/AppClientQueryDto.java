package com.navi.net_pay_backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AppClientQueryDto extends QueryDto {
    private List<Long> ids;
    private String search;
    private Long status;
    private Long serviceId;
    private Long collectorId;
    private Long excludeStatus;
}
