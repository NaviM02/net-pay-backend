package com.navi.net_pay_backend.infrastructure.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MethodCollectionDto {
    private Long methodId;
    private String methodName;
    private String methodDescription;
    private BigDecimal total;
    private long count;
}
