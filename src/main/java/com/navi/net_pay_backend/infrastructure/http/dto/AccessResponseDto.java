package com.navi.net_pay_backend.infrastructure.http.dto;

import java.util.List;
import java.util.Set;

public record AccessResponseDto(List<UserRoleResponseDto> roles, Set<String> permissions) {
}
