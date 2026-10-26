package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.model.CollectorAssignmentRequest;
import com.navi.net_pay_backend.application.use_case.AppClientUseCase;
import com.navi.net_pay_backend.domain.dto.AppClientQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.infrastructure.http.dto.AppClientResponseDto;
import com.navi.net_pay_backend.infrastructure.http.mapper.HttpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class AppClientController {

    private final AppClientUseCase appClientUseCase;
    private final HttpMapper httpMapper;

    @GetMapping("/{hashId}")
    public ResponseEntity<AppClientResponseDto> findByHashId(@PathVariable String hashId) {
        return ResponseEntity.ok(httpMapper.toAppClientResponse(appClientUseCase.findByHashId(hashId)));
    }

    @GetMapping
    public ResponseEntity<List<AppClientResponseDto>> findAll(@ModelAttribute AppClientQueryDto queryDto) {
        PaginatedResult<AppClient> result = appClientUseCase.findAll(queryDto);
        List<AppClientResponseDto> clientRes = result.getItems().stream()
                .map(httpMapper::toAppClientResponse)
                .toList();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(result.getTotal())).body(clientRes);
    }

    @PostMapping
    public ResponseEntity<AppClientResponseDto> create(@RequestBody AppClient client) {
        AppClient created = appClientUseCase.create(client);
        return ResponseEntity.status(HttpStatus.CREATED).body(httpMapper.toAppClientResponse(created));
    }

    @PutMapping("/{hashId}")
    public ResponseEntity<AppClientResponseDto> update(
            @PathVariable String hashId,
            @RequestBody AppClient client
    ) {
        AppClient updated = appClientUseCase.update(hashId, client);
        return ResponseEntity.ok(httpMapper.toAppClientResponse(updated));
    }

    @PutMapping("/{hashId}/collector")
    public ResponseEntity<AppClientResponseDto> assignCollector(
            @PathVariable String hashId,
            @RequestBody CollectorAssignmentRequest request
    ) {
        AppClient updated = appClientUseCase.assignCollectorByHashId(hashId, request.getCollectorHashId());
        return ResponseEntity.ok(httpMapper.toAppClientResponse(updated));
    }

    @DeleteMapping("/{hashId}")
    public ResponseEntity<Void> delete(@PathVariable String hashId) {
        appClientUseCase.delete(hashId);
        return ResponseEntity.noContent().build();
    }
}
