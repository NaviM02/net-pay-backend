package com.navi.net_pay_backend.domain.entity;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class LoginChallenge {
    private String hashId;
    private Long userId;
    private String codeHash;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private int attempts;
    private boolean consumed;
    private String fingerprint;
    private String userAgent;
    private String email;
    private String passwordHash;
}
