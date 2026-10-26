package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.AppReceiptQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppPayment;
import com.navi.net_pay_backend.domain.entity.ClientCharge;
import com.navi.net_pay_backend.domain.entity.Receipt;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AppPaymentRepository;
import com.navi.net_pay_backend.domain.repository.ClientChargeRepository;
import com.navi.net_pay_backend.domain.repository.ReceiptRepository;
import com.navi.net_pay_backend.domain.service.AuditLogService;
import com.navi.net_pay_backend.domain.service.ReceiptNumberGenerator;
import com.navi.net_pay_backend.domain.service.ReceiptPdfRenderer;
import com.navi.net_pay_backend.domain.service.ReceiptStorageService;
import com.navi.net_pay_backend.domain.service.model.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiptUseCase {

    private final ReceiptRepository receiptRepository;
    private final AppPaymentRepository paymentRepository;
    private final ClientChargeRepository clientChargeRepository;
    private final ReceiptNumberGenerator receiptNumberGenerator;
    private final ReceiptPdfRenderer receiptPdfRenderer;
    private final ReceiptStorageService receiptStorageService;
    private final AuditLogService auditLogService;

    public Receipt findByHashId(String hashId) {
        return receiptRepository.findByHashId(hashId)
                .orElseThrow(() -> new EntityNotFoundException("receipt_not_found"));
    }

    public Receipt findByPaymentHashId(String paymentHashId) {
        AppPayment payment = paymentRepository.findByHashIdAndStatusNot(paymentHashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("payment_not_found"));
        return receiptRepository.findByPaymentId(payment.getId())
                .orElseThrow(() -> new EntityNotFoundException("receipt_not_found"));
    }

    public PaginatedResult<Receipt> findAll(AppReceiptQueryDto queryDto) {
        queryDto.setExcludeStatus(TypologyEnum.DELETED.getId());
        return receiptRepository.findAll(queryDto);
    }

    @Transactional
    public byte[] resolvePdf(Receipt receipt) {
        if (receipt.getDocumentPath() != null && receipt.getPublicId() != null
                && receiptStorageService.isConfigured()) {
            return receiptStorageService.download(receipt.getPublicId());
        }

        byte[] pdf = receiptPdfRenderer.render(receipt);

        if (receipt.getDocumentPath() == null && receiptStorageService.isConfigured()) {
            StoredFile stored = receiptStorageService.store(pdf, "receipt-" + receipt.getReceiptNumber() + ".pdf");
            receiptRepository.save(receipt.toBuilder()
                    .documentPath(stored.getUrl())
                    .publicId(stored.getPublicId())
                    .build());
        }
        return pdf;
    }

    @Transactional
    public byte[] resolvePdfByPaymentHashId(String paymentHashId) {
        return resolvePdf(findByPaymentHashId(paymentHashId));
    }

    @Transactional
    public byte[] resolvePdfByHashId(String hashId) {
        return resolvePdf(findByHashId(hashId));
    }

    @Transactional
    public Receipt issueForPayment(AppPayment payment, ClientCharge charge) {
        return issueForPayment(payment, charge, true);
    }

    @Transactional
    public Receipt issueForPayment(AppPayment payment, ClientCharge charge, boolean storeDocument) {
        return receiptRepository.findByPaymentId(payment.getId())
                .orElseGet(() -> createReceipt(payment, charge, storeDocument));
    }

    @Transactional
    public Receipt markPrinted(String hashId) {
        Receipt receipt = findByHashId(hashId);
        if (receipt.getPrintedAt() != null) {
            return receipt;
        }
        LocalDateTime printedAt = LocalDateTime.now();
        Receipt saved = receiptRepository.save(receipt.toBuilder()
                .printedAt(printedAt)
                .build());
        auditLogService.record("receipt_printed", "Imprimió recibo " + saved.getReceiptNumber(), "RECEIPT",
                saved.getHashId(), Map.of("after", Map.of(
                        "receiptNumber", saved.getReceiptNumber(),
                        "printedAt", printedAt.toString()
                )));
        return saved;
    }

    private Receipt createReceipt(AppPayment payment, ClientCharge charge, boolean storeDocument) {
        String number = receiptNumberGenerator.next();

        Receipt draft = Receipt.builder()
                .hashId(UUID.randomUUID().toString())
                .receiptNumber(number)
                .payment(payment)
                .client(payment.getClient())
                .period(charge != null ? charge.getPeriod() : null)
                .amount(payment.getAmount())
                .issuedAt(LocalDateTime.now())
                .collector(payment.getCollector())
                .build();

        String documentPath = null;
        String publicId = null;
        if (storeDocument && receiptStorageService.isConfigured()) {
            byte[] pdf = receiptPdfRenderer.render(draft);
            StoredFile stored = receiptStorageService.store(pdf, "receipt-" + number + ".pdf");
            documentPath = stored.getUrl();
            publicId = stored.getPublicId();
        }

        Receipt saved = receiptRepository.save(draft.toBuilder()
                .documentPath(documentPath)
                .publicId(publicId)
                .build());

        auditLogService.record("receipt_issued", "Emitió recibo " + number, "RECEIPT", saved.getHashId(),
                Map.of("after", Map.of(
                        "receiptNumber", number,
                        "amount", payment.getAmount(),
                        "paymentHashId", payment.getHashId()
                )));
        return saved;
    }
}
