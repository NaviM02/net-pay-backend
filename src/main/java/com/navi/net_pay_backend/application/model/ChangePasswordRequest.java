package com.navi.net_pay_backend.application.model;

public record ChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {
}
