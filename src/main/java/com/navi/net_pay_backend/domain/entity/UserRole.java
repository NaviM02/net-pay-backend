package com.navi.net_pay_backend.domain.entity;

import java.time.Instant;

public record UserRole(String hashId, Role role, Instant startsAt, Instant endsAt, Instant cancelledAt) {
    public boolean isEffectiveAt(Instant instant) {
        return cancelledAt == null && role.isAssignable()
                && !startsAt.isAfter(instant) && (endsAt == null || instant.isBefore(endsAt));
    }
}
