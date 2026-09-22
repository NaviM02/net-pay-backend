package com.navi.net_pay_backend.domain.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class PaymentAllocation {
    private Long id;
    private AppPayment payment;
    private ClientCharge charge;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}
