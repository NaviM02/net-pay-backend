package com.navi.net_pay_backend.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
    private String confirmPassword;
}
