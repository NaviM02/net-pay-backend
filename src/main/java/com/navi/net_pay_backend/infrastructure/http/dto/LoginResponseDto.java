package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.application.model.LoginResponse;
import java.time.LocalDateTime;

public record LoginResponseDto(String authc, boolean twoFactorRequired, String challengeId, LocalDateTime expiresAt) {
    public static LoginResponseDto from(LoginResponse response) {
        return new LoginResponseDto(response.getAuthc(), response.isTwoFactorRequired(),
                response.getChallengeId(), response.getExpiresAt());
    }
}
