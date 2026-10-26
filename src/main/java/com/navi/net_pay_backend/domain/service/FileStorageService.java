package com.navi.net_pay_backend.domain.service;

import com.navi.net_pay_backend.domain.service.model.StoredFile;
import com.navi.net_pay_backend.domain.service.model.DownloadedFile;

public interface FileStorageService {
    boolean isConfigured();
    StoredFile store(byte[] content);
    DownloadedFile download(String url);
}
