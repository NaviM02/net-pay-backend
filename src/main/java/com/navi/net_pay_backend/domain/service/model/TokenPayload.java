package com.navi.net_pay_backend.domain.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenPayload {
    private Long userId;
    private Long roleId;
}