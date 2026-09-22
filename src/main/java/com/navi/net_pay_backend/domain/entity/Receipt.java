package com.navi.net_pay_backend.domain.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@ToString
public class Receipt {
    private Long id;
    private String hashId;
    private String receiptNumber;
    private AppPayment payment;
    private AppClient client;
    private BillingPeriod period;
    private BigDecimal amount;
    private LocalDateTime issuedAt;
    private AppUser collector;
    private String documentPath;
    private String publicId;
    private LocalDateTime printedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
