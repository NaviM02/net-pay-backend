package com.navi.net_pay_backend.infrastructure.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.service.FileStorageService;
import com.navi.net_pay_backend.domain.service.model.StoredFile;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryFileStorageService implements FileStorageService {
    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${cloudinary.uploads-folder:netpay/uploads}")
    private String folder;

    private Cloudinary cloudinary;

    @PostConstruct
    void init() {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) return;
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    @Override
    public boolean isConfigured() {
        return cloudinary != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public StoredFile store(byte[] content) {
        if (cloudinary == null) throw new DomainException("file_storage_not_configured");
        try {
            Map<String, Object> result = cloudinary.uploader().upload(content, ObjectUtils.asMap(
                    "resource_type", "auto",
                    "folder", folder,
                    "public_id", UUID.randomUUID().toString(),
                    "overwrite", false));
            return new StoredFile((String) result.get("secure_url"), (String) result.get("public_id"));
        } catch (Exception ex) {
            throw new DomainException("file_upload_failed");
        }
    }
}
