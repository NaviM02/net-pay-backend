package com.navi.net_pay_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportTable {
    private String title;
    private String subtitle;
    private List<String> headers;
    private List<List<String>> rows;
    private List<Integer> rightAlignedColumns;
}
