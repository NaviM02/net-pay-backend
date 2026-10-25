package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.service.FileStorageService;
import com.navi.net_pay_backend.domain.service.model.DownloadedFile;
import com.navi.net_pay_backend.domain.service.model.StoredFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;

class FileUploadUseCaseTest {
    private final FileStorageService storage = mock(FileStorageService.class);
    private final FileUploadUseCase useCase = new FileUploadUseCase(storage);

    @Test
    void uploadsAndReturnsTheSecureCloudinaryUrl() {
        byte[] file = {1, 2, 3};
        when(storage.isConfigured()).thenReturn(true);
        when(storage.store(file)).thenReturn(new StoredFile(
                "https://res.cloudinary.com/sample/raw/upload/netpay/uploads/test.pdf", "netpay/uploads/test"));

        assertEquals("https://res.cloudinary.com/sample/raw/upload/netpay/uploads/test.pdf", useCase.upload(file));
        verify(storage).store(file);
    }

    @Test
    void rejectsMissingOversizedOrUnconfiguredUploadsBeforeStorage() {
        when(storage.isConfigured()).thenReturn(false);
        assertEquals("file_required", assertThrows(DomainException.class, () -> useCase.upload(null)).getMessage());
        assertEquals("file_too_large", assertThrows(DomainException.class,
                () -> useCase.upload(new byte[FileUploadUseCase.MAX_SIZE_BYTES + 1])).getMessage());
        assertEquals("file_storage_not_configured", assertThrows(DomainException.class,
                () -> useCase.upload(new byte[]{1})).getMessage());
        verify(storage, never()).store(any());
    }

    @Test
    void downloadsStoredAttachmentsAsFileContent() {
        String url = "https://res.cloudinary.com/sample/raw/upload/netpay/uploads/proof.pdf";
        var file = new DownloadedFile(new byte[]{4, 5, 6}, "application/pdf", "proof.pdf");
        when(storage.download(url)).thenReturn(file);

        assertSame(file, useCase.download(url));
        verify(storage).download(url);
    }

    @Test
    void rejectsAnEmptyAttachmentUrlBeforeStorage() {
        assertEquals("payment_attachment_not_found", assertThrows(DomainException.class,
                () -> useCase.download(" ")).getMessage());
        verify(storage, never()).download(anyString());
    }
}
