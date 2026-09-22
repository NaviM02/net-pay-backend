package com.navi.net_pay_backend.domain.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor @ToString
public class AppUser {
    private Long id;
    private String hashId;
    private String fullName;
    private String email;
    private String password;
    private AdmTypology tpRole;
    private AdmTypology tpStatus;
    private LocalDateTime lastLoginAt;
}