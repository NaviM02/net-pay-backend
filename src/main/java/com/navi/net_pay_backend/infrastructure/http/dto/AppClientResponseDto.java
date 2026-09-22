package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.domain.entity.AdmTypology;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class AppClientResponseDto {
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
    private AppServiceResponseDto service;
    private AppUserResponseDto collector;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
