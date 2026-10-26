package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.ClientAccountUseCase;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.dto.QueryDto;
import com.navi.net_pay_backend.infrastructure.http.dto.ClientAccountResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.ClientChargeResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.ClientPaymentStatusResponseDto;
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
    public ResponseEntity<List<ClientChargeResponseDto>> charges(@PathVariable String hashId,
                                                                  @ModelAttribute QueryDto query) {
        PaginatedResult<com.navi.net_pay_backend.domain.dto.ClientChargeView> result = clientAccountUseCase.charges(hashId, query);
        List<ClientChargeResponseDto> charges = result.getItems().stream()
                .map(httpMapper::toClientChargeResponse)
                .toList();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(result.getTotal())).body(charges);
    }

    @GetMapping("/{hashId}/account")
    public ResponseEntity<ClientAccountResponseDto> account(@PathVariable String hashId, @ModelAttribute QueryDto query) {
        var account = clientAccountUseCase.account(hashId, query);
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(account.getTotalCharges()))
                .body(httpMapper.toClientAccountResponse(account));
    }

    @GetMapping("/{hashId}/payment-status")
    public ResponseEntity<ClientPaymentStatusResponseDto> paymentStatus(@PathVariable String hashId) {
        var account = clientAccountUseCase.account(hashId, new QueryDto());
        String status = account.getOverdueCount() > 0 ? "OVERDUE"
                : account.getCredit().signum() > 0 ? "CREDIT" : "UP_TO_DATE";
        return ResponseEntity.ok(new ClientPaymentStatusResponseDto(account.getClientHashId(), status,
                account.getTotalExpected(), account.getTotalPaid(), account.getBalance(),
                account.getCredit(), account.getOverdueCount()));
    }
}
