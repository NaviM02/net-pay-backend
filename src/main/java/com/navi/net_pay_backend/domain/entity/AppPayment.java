package com.navi.net_pay_backend.domain.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor @ToString
public class AppPayment {
    private Long id;
    private String hashId;
    private AppClient client;
    private AppService service;
    private BigDecimal amount;
    private LocalDate paidAt;
    private AppUser collector;
    private AdmTypology tpPaymentMethod;
    private AdmTypology tpPaymentStatus;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
