package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.domain.entity.AdmTypology;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class AppPaymentResponseDto {
    private Long id;
    private String hashId;
    private AppClientResponseDto client;
    private AppServiceResponseDto service;
    private BigDecimal amount;
    private LocalDate paidAt;
    private AppUserResponseDto collector;
    private AdmTypology tpPaymentMethod;
    private AdmTypology tpPaymentStatus;
    private String reference;
    private String comment;
    private List<AppPaymentAllocationDto> allocations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
