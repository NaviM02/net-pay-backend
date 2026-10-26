package com.navi.net_pay_backend.domain.entity;

public record Permission(String hashId, String resource, String action) {
    public String authority() {
        return resource + ":" + action;
    }
}
