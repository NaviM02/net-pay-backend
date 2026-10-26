package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.application.model.ChangePasswordRequest;

public record ChangePasswordRequestDto(String currentPassword, String newPassword, String confirmPassword) {
    public ChangePasswordRequest toRequest() {
        return new ChangePasswordRequest(currentPassword, newPassword, confirmPassword);
    }
}
