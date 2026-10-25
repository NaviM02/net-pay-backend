package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.application.model.UserRoleRequest;
import java.time.Instant;

public record UserRoleRequestDto(String roleHashId, Instant startsAt, Instant endsAt) {
    public UserRoleRequest toRequest() { return new UserRoleRequest(roleHashId, startsAt, endsAt); }
}
