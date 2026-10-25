package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.AppPaymentUseCase;
import com.navi.net_pay_backend.application.use_case.FileUploadUseCase;
import com.navi.net_pay_backend.application.use_case.ReceiptUseCase;
import com.navi.net_pay_backend.domain.dto.AppPaymentQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppPayment;
import com.navi.net_pay_backend.domain.entity.Receipt;
import com.navi.net_pay_backend.infrastructure.http.dto.AppPaymentResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.ReceiptResponseDto;
import com.navi.net_pay_backend.infrastructure.http.mapper.HttpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class AppPaymentController {

    private final AppPaymentUseCase paymentUseCase;
    private final ReceiptUseCase receiptUseCase;
    private final FileUploadUseCase fileUpload;
    private final HttpMapper httpMapper;

    @GetMapping("/{hashId}")
    public ResponseEntity<AppPaymentResponseDto> findByHashId(@PathVariable String hashId) {
        return ResponseEntity.ok(httpMapper.toAppPaymentResponse(paymentUseCase.findByHashId(hashId)));
    }

    @GetMapping("/{hashId}/attachment")
    public ResponseEntity<byte[]> attachment(@PathVariable String hashId) {
        AppPayment payment = paymentUseCase.findByHashId(hashId);
        var file = fileUpload.download(payment.getAttachmentUrl());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        org.springframework.http.ContentDisposition.inline()
                                .filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.content());
    }

    @GetMapping
    public ResponseEntity<List<AppPaymentResponseDto>> findAll(@ModelAttribute AppPaymentQueryDto queryDto) {
        PaginatedResult<AppPayment> result = paymentUseCase.findAll(queryDto);
        List<AppPaymentResponseDto> paymentRes = result.getItems().stream()
                .map(httpMapper::toAppPaymentResponse)
                .toList();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(result.getTotal())).body(paymentRes);
    }

    @PostMapping
    public ResponseEntity<AppPaymentResponseDto> create(@RequestBody AppPayment payment) {
        AppPayment created = paymentUseCase.create(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(httpMapper.toAppPaymentResponse(created));
    }

    @PostMapping("/collector")
    public ResponseEntity<AppPaymentResponseDto> createAsCollector(@RequestBody AppPayment payment) {
        AppPayment created = paymentUseCase.createAsCollector(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(httpMapper.toAppPaymentResponse(created));
    }

    @PutMapping("/{hashId}")
    public ResponseEntity<AppPaymentResponseDto> update(
            @PathVariable String hashId,
            @RequestBody AppPayment payment
    ) {
        AppPayment updated = paymentUseCase.update(hashId, payment);
        return ResponseEntity.ok(httpMapper.toAppPaymentResponse(updated));
    }

    @GetMapping("/{hashId}/receipt")
    public ResponseEntity<ReceiptResponseDto> receipt(@PathVariable String hashId) {
        return ResponseEntity.ok(httpMapper.toReceiptResponse(receiptUseCase.findByPaymentHashId(hashId)));
    }

    @GetMapping("/{hashId}/receipt/pdf")
    public ResponseEntity<byte[]> receiptPdf(@PathVariable String hashId) {
        Receipt receipt = receiptUseCase.findByPaymentHashId(hashId);
        byte[] pdf = receiptUseCase.resolvePdf(receipt);
        return ReceiptController.pdfResponse(pdf, receipt.getReceiptNumber());
    }

    @DeleteMapping("/{hashId}")
    public ResponseEntity<Void> delete(@PathVariable String hashId) {
        paymentUseCase.delete(hashId);
        return ResponseEntity.noContent().build();
    }
}
