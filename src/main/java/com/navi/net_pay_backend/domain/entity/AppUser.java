package com.navi.net_pay_backend.domain.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor @ToString
public class AppUser {
    private Long id;
    private String hashId;
    private String fullName;
    private String email;
    private String password;
    private AdmTypology tpStatus;
    private LocalDateTime lastLoginAt;
    private String resetTokenHash;
    private LocalDateTime resetTokenExpiresAt;
}
