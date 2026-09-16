package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.domain.entity.AdmTypology;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class AppUserResponseDto {
    private Long id;
    private String hashId;
    private String fullName;
    private String email;
    private AdmTypology tpRole;
    private AdmTypology tpStatus;
    private LocalDateTime lastLoginAt;
}
