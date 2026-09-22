package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.domain.entity.AdmTypology;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class AppServiceResponseDto {
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
}
