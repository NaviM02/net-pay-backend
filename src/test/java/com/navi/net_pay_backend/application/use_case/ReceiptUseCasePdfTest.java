package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.entity.Receipt;
import com.navi.net_pay_backend.domain.repository.ReceiptRepository;
import com.navi.net_pay_backend.domain.service.ReceiptPdfRenderer;
import com.navi.net_pay_backend.domain.service.ReceiptStorageService;
import com.navi.net_pay_backend.domain.service.model.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptUseCasePdfTest {

    @Mock ReceiptRepository receiptRepository;
    @Mock ReceiptPdfRenderer receiptPdfRenderer;
    @Mock ReceiptStorageService receiptStorageService;
    @Mock com.navi.net_pay_backend.domain.repository.AppPaymentRepository paymentRepository;
    @Mock com.navi.net_pay_backend.domain.repository.ClientChargeRepository clientChargeRepository;
    @Mock com.navi.net_pay_backend.domain.service.AuditLogService auditLogService;
    @Mock com.navi.net_pay_backend.domain.service.ReceiptNumberGenerator receiptNumberGenerator;

    @InjectMocks ReceiptUseCase receiptUseCase;

    private static final byte[] GENERATED = {1, 2, 3};
    private static final byte[] REMOTE = {9, 8, 7};

    private Receipt receipt(String documentPath, String publicId) {
        return Receipt.builder()
                .id(1L)
                .hashId("h")
                .receiptNumber("REC-2026-000001")
                .amount(BigDecimal.TEN)
                .issuedAt(LocalDateTime.now())
                .documentPath(documentPath)
                .publicId(publicId)
                .build();
    }

    @Test
    void documentedReceipt_downloadsFromStorage_andDoesNotRegenerate() {
        when(receiptStorageService.isConfigured()).thenReturn(true);
        when(receiptStorageService.download("netpay/receipts/receipt-REC-2026-000001")).thenReturn(REMOTE);

        byte[] result = receiptUseCase.resolvePdf(
                receipt("https://cdn/x.pdf", "netpay/receipts/receipt-REC-2026-000001"));

        assertThat(result).isEqualTo(REMOTE);
        verify(receiptStorageService).download("netpay/receipts/receipt-REC-2026-000001");
        verify(receiptPdfRenderer, never()).render(any());
        verify(receiptRepository, never()).save(any());
    }

    @Test
    void undocumentedReceipt_withStorage_backfillsAndReturnsBytes() {
        when(receiptStorageService.isConfigured()).thenReturn(true);
        when(receiptPdfRenderer.render(any())).thenReturn(GENERATED);
        when(receiptStorageService.store(any(), anyString()))
                .thenReturn(new StoredFile("https://cdn/x.pdf", "netpay/receipts/receipt-REC-2026-000001"));

        byte[] result = receiptUseCase.resolvePdf(receipt(null, null));

        assertThat(result).isEqualTo(GENERATED);
        verify(receiptRepository).save(any());
        verify(receiptStorageService).store(any(), anyString());
    }

    @Test
    void undocumentedReceipt_withoutStorage_generatesOnly() {
        when(receiptStorageService.isConfigured()).thenReturn(false);
        when(receiptPdfRenderer.render(any())).thenReturn(GENERATED);

        byte[] result = receiptUseCase.resolvePdf(receipt(null, null));

        assertThat(result).isEqualTo(GENERATED);
        verify(receiptPdfRenderer).render(any());
        verify(receiptStorageService, never()).store(any(), anyString());
        verify(receiptRepository, never()).save(any());
    }
}
