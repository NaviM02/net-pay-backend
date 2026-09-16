package com.navi.net_pay_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter @Setter @AllArgsConstructor
public class PaginatedResult<T> {
    private List<T> items;
    private long total;
}
