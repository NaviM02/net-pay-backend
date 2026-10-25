package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.application.model.RoleRequest;
import java.util.List;

public record RoleRequestDto(String name, String description, String status, List<String> permissionHashIds) {
    public RoleRequest toRequest() { return new RoleRequest(name, description, status, permissionHashIds); }
}
