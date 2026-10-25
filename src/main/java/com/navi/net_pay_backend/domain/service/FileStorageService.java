package com.navi.net_pay_backend.domain.service;

import com.navi.net_pay_backend.domain.service.model.StoredFile;

public interface FileStorageService {
    boolean isConfigured();
    StoredFile store(byte[] content);
}
