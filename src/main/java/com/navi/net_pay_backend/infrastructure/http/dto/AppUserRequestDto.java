package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.application.model.AppUserRequest;

public record AppUserRequestDto(String fullName, String email, String password, Long status) {
    public AppUserRequest toRequest() { return new AppUserRequest(fullName, email, password, status); }
}
