package com.navi.net_pay_backend.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String authc;
    private boolean twoFactorRequired;
    private String challengeId;
    private LocalDateTime expiresAt;

    public LoginResponse(String authc) {
        this(authc, false, null, null);
    }
}
