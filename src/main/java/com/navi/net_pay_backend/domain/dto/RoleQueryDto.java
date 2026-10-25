package com.navi.net_pay_backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleQueryDto extends QueryDto {
    private String search;
    private String status;
}
