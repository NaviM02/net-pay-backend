package com.navi.net_pay_backend.infrastructure.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptResponseDto {
    private String hashId;
    private String receiptNumber;
    private String paymentHashId;
    private String clientHashId;
    private String clientName;
    private String periodLabel;
    private BigDecimal amount;
    private LocalDateTime issuedAt;
    private String collectorName;
    private String documentPath;
    private LocalDateTime printedAt;
}
