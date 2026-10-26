package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.domain.entity.Role;
import java.util.List;

public record RoleResponseDto(String hashId, String name, String description, String status, Long statusInternalId, List<PermissionResponseDto> permissions) {
    public static RoleResponseDto from(Role role) {
        return new RoleResponseDto(role.hashId(), role.name(), role.description(), role.status(), role.statusInternalId(),
                role.permissions().stream().map(PermissionResponseDto::from).toList());
    }
}
