package com.navi.net_pay_backend.application.model;

import java.time.Instant;

public record UserRoleRequest(String roleHashId, Instant startsAt, Instant endsAt) {
}
