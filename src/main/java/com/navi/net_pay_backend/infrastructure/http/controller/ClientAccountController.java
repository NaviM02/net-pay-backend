package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.ClientAccountUseCase;
import com.navi.net_pay_backend.infrastructure.http.dto.ClientAccountResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.ClientChargeResponseDto;
import com.navi.net_pay_backend.infrastructure.http.mapper.HttpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientAccountController {

    private final ClientAccountUseCase clientAccountUseCase;
    private final HttpMapper httpMapper;

    @GetMapping("/{hashId}/charges")
    public ResponseEntity<List<ClientChargeResponseDto>> charges(@PathVariable String hashId) {
        List<ClientChargeResponseDto> charges = clientAccountUseCase.charges(hashId).stream()
                .map(httpMapper::toClientChargeResponse)
                .toList();
        return ResponseEntity.ok(charges);
    }

    @GetMapping("/{hashId}/account")
    public ResponseEntity<ClientAccountResponseDto> account(@PathVariable String hashId) {
        return ResponseEntity.ok(httpMapper.toClientAccountResponse(clientAccountUseCase.account(hashId)));
    }
}
