package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.AuditLogUseCase;
import com.navi.net_pay_backend.domain.dto.AuditLogQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AuditLog;
import com.navi.net_pay_backend.infrastructure.http.dto.AuditLogResponseDto;
import com.navi.net_pay_backend.infrastructure.http.mapper.HttpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogUseCase auditLogUseCase;
    private final HttpMapper httpMapper;

    @GetMapping
    public ResponseEntity<List<AuditLogResponseDto>> findAll(@ModelAttribute AuditLogQueryDto queryDto) {
        PaginatedResult<AuditLog> result = auditLogUseCase.findAll(queryDto);
        List<AuditLogResponseDto> response = result.getItems().stream()
                .map(httpMapper::toAuditLogResponse)
                .toList();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(result.getTotal())).body(response);
    }

    @GetMapping("/entity/{entityType}/{entityHashId}")
    public ResponseEntity<List<AuditLogResponseDto>> findByEntity(
            @PathVariable String entityType,
            @PathVariable String entityHashId
    ) {
        List<AuditLogResponseDto> response = auditLogUseCase.findByEntity(entityType.toUpperCase(), entityHashId).stream()
                .map(httpMapper::toAuditLogResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
