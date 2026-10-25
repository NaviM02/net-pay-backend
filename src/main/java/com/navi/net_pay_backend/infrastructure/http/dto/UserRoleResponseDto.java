package com.navi.net_pay_backend.infrastructure.http.dto;

import com.navi.net_pay_backend.domain.entity.UserRole;
import java.time.Instant;

public record UserRoleResponseDto(String hashId, RoleResponseDto role, Instant startsAt, Instant endsAt,
                                  Instant cancelledAt, boolean effective) {
    public static UserRoleResponseDto from(UserRole assignment) {
        return new UserRoleResponseDto(assignment.hashId(), RoleResponseDto.from(assignment.role()), assignment.startsAt(),
                assignment.endsAt(), assignment.cancelledAt(), assignment.isEffectiveAt(Instant.now()));
    }
}
