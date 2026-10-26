package com.navi.net_pay_backend.infrastructure.http.dto;

public record ProfileUpdateRequestDto(String fullName, String email, Boolean twoFactorEnabled) {
}
