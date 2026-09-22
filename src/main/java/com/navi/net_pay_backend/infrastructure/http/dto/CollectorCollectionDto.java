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
public class CollectorCollectionDto {
    private String collectorHashId;
    private String collectorName;
    private BigDecimal totalCollected;
    private long paymentCount;
}
