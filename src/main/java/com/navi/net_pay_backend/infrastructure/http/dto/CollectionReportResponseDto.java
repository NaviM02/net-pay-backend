package com.navi.net_pay_backend.infrastructure.http.dto;

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
public class CollectionReportResponseDto {
    private LocalDate fromDate;
    private LocalDate toDate;
    private CollectionSummaryDto summary;
    private List<CollectorCollectionDto> byCollector;
}
