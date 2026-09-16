package com.navi.net_pay_backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class AppUserQueryDto extends QueryDto {
    private List<Long> ids;
    private String email;
    private String fullName;
    private Long status;
    private Long excludeStatus;
}
