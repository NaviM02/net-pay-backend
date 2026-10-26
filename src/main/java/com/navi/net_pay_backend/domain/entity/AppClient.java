package com.navi.net_pay_backend.domain.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor @ToString
public class AppClient {
    private Long id;
    private String hashId;
    private String businessName;
    private String contactName;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private LocalDate customerSince;
    private AdmTypology tpStatus;
    private AppService service;
    private AppUser collector;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
