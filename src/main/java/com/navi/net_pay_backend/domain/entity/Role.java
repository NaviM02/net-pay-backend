package com.navi.net_pay_backend.domain.entity;

import java.util.List;

public record Role(String hashId, String name, String description, String status, List<Permission> permissions) {
    public Long statusInternalId() {
        return com.navi.net_pay_backend.domain.enums.TypologyEnum.valueOf(status).getId();
    }

    public boolean isAssignable() {
        return "ACTIVE".equals(status) || "LOCKED".equals(status);
    }
}
