package com.navi.net_pay_backend.domain.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class ClientCharge {
    private Long id;
    private String hashId;
    private AppClient client;
    private BillingPeriod period;
    private AppService service;
    private BigDecimal expectedAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
