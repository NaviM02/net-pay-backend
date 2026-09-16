package com.navi.net_pay_backend.domain.enums;

import lombok.Getter;

@Getter
public enum TypologyEnum {
    ACTIVE(49L),
    INACTIVE(30L),
    DELETED(51L),
    LOCKED(52L),
    SUSPENDED(53L);

    private final Long id;

    TypologyEnum(Long id) {
        this.id = id;
    }

}
