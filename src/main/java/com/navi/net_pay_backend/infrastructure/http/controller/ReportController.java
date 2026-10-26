package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.ReportUseCase;
import com.navi.net_pay_backend.domain.dto.CollectionReportView;
import com.navi.net_pay_backend.domain.dto.OverdueClientQueryDto;
import com.navi.net_pay_backend.domain.dto.OverdueClientView;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.infrastructure.http.dto.CollectionReportResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.OverdueClientResponseDto;
import com.navi.net_pay_backend.infrastructure.http.mapper.HttpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final String PDF = "pdf";
    private static final String XLS = "xls";

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

    @GetMapping("/collections/export")
    public ResponseEntity<byte[]> collectionsExport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "10") int collectorLimit,
            @RequestParam(defaultValue = XLS) String format
    ) {
        String normalized = normalizeFormat(format);
        byte[] body = PDF.equals(normalized)
                ? reportUseCase.exportCollectionsPdf(fromDate, toDate, collectorLimit)
                : reportUseCase.exportCollectionsXls(fromDate, toDate, collectorLimit);
        return exportResponse(body, normalized, "reporte-cobros-por-periodo");
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

    @GetMapping("/overdue-clients/export")
    public ResponseEntity<byte[]> overdueClientsExport(
            @ModelAttribute OverdueClientQueryDto queryDto,
            @RequestParam(defaultValue = XLS) String format
    ) {
        String normalized = normalizeFormat(format);
        byte[] body = PDF.equals(normalized)
                ? reportUseCase.exportOverdueClientsPdf(queryDto)
                : reportUseCase.exportOverdueClientsXls(queryDto);
        return exportResponse(body, normalized, "reporte-clientes-en-mora");
    }

    private String normalizeFormat(String format) {
        if (format == null) {
            return XLS;
        }
        String normalized = format.trim().toLowerCase();
        if (!PDF.equals(normalized) && !XLS.equals(normalized)) {
            throw new DomainException("unsupported_report_format");
        }
        return normalized;
    }

    private ResponseEntity<byte[]> exportResponse(byte[] body, String format, String basename) {
        HttpHeaders headers = new HttpHeaders();
        if (PDF.equals(format)) {
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(basename + ".pdf").build());
        } else {
            headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(basename + ".xls").build());
        }
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
}
