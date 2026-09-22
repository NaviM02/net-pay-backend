package com.navi.net_pay_backend.domain.entity;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor @ToString
public class AppService {
    private Long id;
    private String hashId;
    private String name;
    private String speed;
    private BigDecimal monthlyPrice;
    private Integer clientCount;
    private AdmTypology tpType;
    private AdmTypology tpStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
