package com.navi.net_pay_backend.domain.enums;

import lombok.Getter;

@Getter
public enum TypologyEnum {
    // ── Status
    ACTIVE(49L, 48L),
    INACTIVE(50L, 48L),
    DELETED(51L, 48L),
    LOCKED(52L, 48L),
    SUSPENDED(53L, 48L),
    // ── Service_Types
    RESIDENTIAL(71L, 70L),
    BUSINESS(72L, 70L),
    CORPORATE(73L, 70L),
    // ── Payment_Methods
    CASH(81L, 80L),
    TRANSFER(82L, 80L),
    CARD(83L, 80L),
    // ── Payment_Status
    COMPLETED(91L, 90L),
    PENDING(92L, 90L),
    FAILED(93L, 90L),
    // ── Audit_Levels
    INFO(101L, 100L),
    WARNING(102L, 100L),
    ERROR(103L, 100L);

    private final Long id;
    private final Long parentId;

    TypologyEnum(Long id, Long parentId) {
        this.id = id;
        this.parentId = parentId;
    }

    public static TypologyEnum findById(Long id) {
        if (id == null) {
            return null;
        }
        for (TypologyEnum value : values()) {
            if (value.id.equals(id)) {
                return value;
            }
        }
        return null;
    }
}
