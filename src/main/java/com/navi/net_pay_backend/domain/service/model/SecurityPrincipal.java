package com.navi.net_pay_backend.domain.service.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SecurityPrincipal {
    private final Long userId;
}
