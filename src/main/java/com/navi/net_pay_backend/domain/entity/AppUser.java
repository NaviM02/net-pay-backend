package com.navi.net_pay_backend.domain.entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AppUser {
    private Long id;
    private String publicId;
    private String fullName;
    private String email;
    private String passwordHash;
    private Long roleId;
    private Long statusId;
    private LocalDateTime lastLoginAt;
}