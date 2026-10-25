package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.domain.entity.Permission;

public record PermissionResponseDto(String hashId, String resource, String action, String authority) {
    public static PermissionResponseDto from(Permission permission) {
        return new PermissionResponseDto(permission.hashId(), permission.resource(), permission.action(), permission.authority());
    }
}
