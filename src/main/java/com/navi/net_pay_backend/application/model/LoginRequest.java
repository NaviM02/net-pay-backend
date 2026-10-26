package com.navi.net_pay_backend.application.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
    private String fingerprint;
}