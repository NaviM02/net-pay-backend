package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileUploadUseCase {
    public static final int MAX_SIZE_BYTES = 10 * 1024 * 1024;

    private final FileStorageService storage;

    public String upload(byte[] content) {
        if (content == null || content.length == 0) throw new DomainException("file_required");
        if (content.length > MAX_SIZE_BYTES) throw new DomainException("file_too_large");
        if (!storage.isConfigured()) throw new DomainException("file_storage_not_configured");
        return storage.store(content).getUrl();
    }
}
