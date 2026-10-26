package com.navi.net_pay_backend.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class TokenCredential {
    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime effectiveDate;
    private LocalDateTime expirationDate;
    private String userAgent;
    private String fingerprint;
}