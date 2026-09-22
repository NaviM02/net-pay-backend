package com.navi.net_pay_backend.infrastructure.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.service.ReceiptStorageService;
import com.navi.net_pay_backend.domain.service.model.StoredFile;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryReceiptStorageService implements ReceiptStorageService {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${cloudinary.folder:netpay/receipts}")
    private String folder;

    private Cloudinary cloudinary;

    @PostConstruct
    void init() {
        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            return;
        }
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    @Override
    public boolean isConfigured() {
        return cloudinary != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public StoredFile store(byte[] content, String fileName) {
        if (cloudinary == null) {
            throw new DomainException("receipt_storage_not_configured");
        }
        try {
            Map<String, Object> result = cloudinary.uploader().upload(content, ObjectUtils.asMap(
                    "resource_type", "raw",
                    "folder", folder,
                    "public_id", fileName.replace(".pdf", ""),
                    "overwrite", true
            ));
            return new StoredFile(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id")
            );
        } catch (Exception ex) {
            throw new DomainException("receipt_storage_failed");
        }
    }

    @Override
    public byte[] download(String publicId) {
        if (cloudinary == null) {
            throw new DomainException("receipt_storage_not_configured");
        }
        if (publicId == null || publicId.isBlank()) {
            throw new DomainException("receipt_document_not_stored");
        }
        try {
            String url = "https://res.cloudinary.com/" + cloudName + "/raw/upload/" + publicId;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                throw new DomainException("receipt_document_download_failed");
            }
            return response.body();
        } catch (DomainException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DomainException("receipt_document_download_failed");
        }
    }
}
