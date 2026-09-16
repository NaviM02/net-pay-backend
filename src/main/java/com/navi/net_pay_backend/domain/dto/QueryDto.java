package com.navi.net_pay_backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class QueryDto {
    private Integer offset = 0;
    private Integer size = 10;
    private String columnOrder;
    private boolean asc = true;
}
