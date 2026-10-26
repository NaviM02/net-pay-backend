package com.navi.net_pay_backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CollectionReportView {
    private LocalDate fromDate;
    private LocalDate toDate;
    private CollectionSummary summary;
    private List<CollectorCollection> byCollector;
}
