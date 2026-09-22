package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.ReceiptUseCase;
import com.navi.net_pay_backend.domain.dto.AppReceiptQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.Receipt;
import com.navi.net_pay_backend.infrastructure.http.dto.ReceiptResponseDto;
import com.navi.net_pay_backend.infrastructure.http.mapper.HttpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptUseCase receiptUseCase;
    private final HttpMapper httpMapper;

    @GetMapping
    public ResponseEntity<List<ReceiptResponseDto>> findAll(@ModelAttribute AppReceiptQueryDto queryDto) {
        PaginatedResult<Receipt> result = receiptUseCase.findAll(queryDto);
        List<ReceiptResponseDto> response = result.getItems().stream()
                .map(httpMapper::toReceiptResponse)
                .toList();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(result.getTotal())).body(response);
    }

    @GetMapping("/{hashId}")
    public ResponseEntity<ReceiptResponseDto> findByHashId(@PathVariable String hashId) {
        return ResponseEntity.ok(httpMapper.toReceiptResponse(receiptUseCase.findByHashId(hashId)));
    }

    @GetMapping("/{hashId}/document")
    public ResponseEntity<byte[]> document(@PathVariable String hashId) {
        Receipt receipt = receiptUseCase.findByHashId(hashId);
        byte[] pdf = receiptUseCase.resolvePdf(receipt);
        return pdfResponse(pdf, receipt.getReceiptNumber());
    }

    @PostMapping("/{hashId}/print")
    public ResponseEntity<ReceiptResponseDto> print(@PathVariable String hashId) {
        return ResponseEntity.ok(httpMapper.toReceiptResponse(receiptUseCase.markPrinted(hashId)));
    }

    static ResponseEntity<byte[]> pdfResponse(byte[] pdf, String receiptNumber) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", "recibo-" + receiptNumber + ".pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
