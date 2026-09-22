package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.ReportUseCase;
import com.navi.net_pay_backend.domain.dto.CollectionReportView;
import com.navi.net_pay_backend.domain.dto.OverdueClientQueryDto;
import com.navi.net_pay_backend.domain.dto.OverdueClientView;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.infrastructure.http.dto.CollectionReportResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.OverdueClientResponseDto;
import com.navi.net_pay_backend.infrastructure.http.mapper.HttpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportUseCase reportUseCase;
    private final HttpMapper httpMapper;

    @GetMapping("/collections")
    public ResponseEntity<CollectionReportResponseDto> collections(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "10") int collectorLimit
    ) {
        CollectionReportView view = reportUseCase.collections(fromDate, toDate, collectorLimit);
        return ResponseEntity.ok(httpMapper.toCollectionReportResponse(view));
    }

    @GetMapping("/overdue-clients")
    public ResponseEntity<List<OverdueClientResponseDto>> overdueClients(
            @ModelAttribute OverdueClientQueryDto queryDto
    ) {
        PaginatedResult<OverdueClientView> result = reportUseCase.overdueClients(queryDto);
        List<OverdueClientResponseDto> response = result.getItems().stream()
                .map(httpMapper::toOverdueClientResponse)
                .toList();
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(result.getTotal()))
                .body(response);
    }
}
