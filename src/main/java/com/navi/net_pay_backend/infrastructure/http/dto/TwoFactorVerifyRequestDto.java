package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.application.model.TwoFactorVerifyRequest;

public record TwoFactorVerifyRequestDto(String challengeId, String code) {
    public TwoFactorVerifyRequest toRequest() {
        return new TwoFactorVerifyRequest(challengeId, code);
    }
}
