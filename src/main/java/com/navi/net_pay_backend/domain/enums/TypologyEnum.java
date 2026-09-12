package com.navi.net_pay_backend.domain.enums;

import lombok.Getter;

@Getter
public enum TypologyEnum {
    ACTIVE(30049L),
    INACTIVE(30050L),
    DELETED(30051L),
    LOCKED(30052L),
    SUSPENDED(30053L);

    private final Long id;

    TypologyEnum(Long id) {
        this.id = id;
    }

}
