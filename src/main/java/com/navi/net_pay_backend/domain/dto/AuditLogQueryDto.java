package com.navi.net_pay_backend.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class AuditLogQueryDto extends QueryDto {
    private String search;
    private Long level;
    private Long userId;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    public AuditLogQueryDto() {
        setAsc(false);
    }
}
