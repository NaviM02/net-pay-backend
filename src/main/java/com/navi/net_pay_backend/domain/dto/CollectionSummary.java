package com.navi.net_pay_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionSummary {
    private BigDecimal totalCollected;
    private long completedCount;
    private long failedCount;
    private long pendingCount;
    private BigDecimal successRate;
}
