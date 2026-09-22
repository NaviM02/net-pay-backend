package com.navi.net_pay_backend.infrastructure.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyMethodCollectionDto {
    private int year;
    private int month;
    private String label;
    private List<MethodCollectionDto> methods;
}
