package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.AppServiceUseCase;
import com.navi.net_pay_backend.domain.dto.AppServiceQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.infrastructure.http.dto.AppServiceResponseDto;
import com.navi.net_pay_backend.infrastructure.http.mapper.HttpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class AppServiceController {

    private final AppServiceUseCase serviceUseCase;
    private final HttpMapper httpMapper;

    @GetMapping("/{hashId}")
    public ResponseEntity<AppServiceResponseDto> findByHashId(@PathVariable String hashId) {
        return ResponseEntity.ok(httpMapper.toAppServiceResponse(serviceUseCase.findByHashId(hashId)));
    }

    @GetMapping
    public ResponseEntity<List<AppServiceResponseDto>> findAll(@ModelAttribute AppServiceQueryDto queryDto) {
        PaginatedResult<AppService> result = serviceUseCase.findAll(queryDto);
        List<AppServiceResponseDto> serviceRes = result.getItems().stream()
                .map(httpMapper::toAppServiceResponse)
                .toList();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(result.getTotal())).body(serviceRes);
    }

    @PostMapping
    public ResponseEntity<AppServiceResponseDto> create(@RequestBody AppService service) {
        AppService created = serviceUseCase.create(service);
        return ResponseEntity.status(HttpStatus.CREATED).body(httpMapper.toAppServiceResponse(created));
    }

    @PutMapping("/{hashId}")
    public ResponseEntity<AppServiceResponseDto> update(
            @PathVariable String hashId,
            @RequestBody AppService service
    ) {
        AppService updated = serviceUseCase.update(hashId, service);
        return ResponseEntity.ok(httpMapper.toAppServiceResponse(updated));
    }

    @DeleteMapping("/{hashId}")
    public ResponseEntity<Void> delete(@PathVariable String hashId) {
        serviceUseCase.delete(hashId);
        return ResponseEntity.noContent().build();
    }
}
