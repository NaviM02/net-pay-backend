package com.navi.net_pay_backend.application.model;

import java.util.List;

public record RoleRequest(String name, String description, String status, List<String> permissionHashIds) {
}
