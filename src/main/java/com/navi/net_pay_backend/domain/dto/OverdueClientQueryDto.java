package com.navi.net_pay_backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OverdueClientQueryDto extends QueryDto {
    private String search;
    private String serviceHashId;
    private Long status;

    public OverdueClientQueryDto() {
        setSize(10);
    }
}
