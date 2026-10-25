package com.navi.net_pay_backend.infrastructure.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.service.FileStorageService;
import com.navi.net_pay_backend.domain.service.model.DownloadedFile;
import com.navi.net_pay_backend.domain.service.model.StoredFile;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryFileStorageService implements FileStorageService {
    private static final String CLOUDINARY_HOST = "res.cloudinary.com";

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
            String publicId = UUID.randomUUID().toString();
            Map<String, Object> result = cloudinary.uploader().upload(content, ObjectUtils.asMap(
                    "resource_type", "raw",
                    "folder", folder,
                    "public_id", publicId,
                    "overwrite", true));
            return new StoredFile((String) result.get("secure_url"), (String) result.get("public_id"));
        } catch (Exception ex) {
            throw new DomainException("file_upload_failed");
        }
    }

    @Override
    public DownloadedFile download(String url) {
        if (cloudinary == null) throw new DomainException("file_storage_not_configured");
        URI uri;
        try {
            uri = URI.create(url);
        } catch (Exception ex) {
            throw new DomainException("payment_attachment_url_invalid");
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())
                || !CLOUDINARY_HOST.equalsIgnoreCase(uri.getHost())
                || uri.getPath() == null
                || !uri.getPath().startsWith("/" + cloudName + "/")) {
            throw new DomainException("payment_attachment_url_invalid");
        }
        try {
            String path = uri.getPath();
            int uploadIndex = path.indexOf("/upload/");
            if (uploadIndex < 0) throw new DomainException("payment_attachment_url_invalid");
            String assetPath = path.substring(uploadIndex + "/upload/".length());
            if (assetPath.matches("v\\d+/.+")) assetPath = assetPath.substring(assetPath.indexOf('/') + 1);
            URI rawUri = URI.create("https://" + CLOUDINARY_HOST + "/" + cloudName + "/raw/upload/" + assetPath);
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder(rawUri).timeout(Duration.ofSeconds(30)).GET().build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) throw new DomainException("payment_attachment_download_failed");
            String contentType = response.headers().firstValue("Content-Type")
                    .map(value -> value.split(";", 2)[0]).orElse("application/octet-stream");
            String fileName = path.substring(path.lastIndexOf('/') + 1).replaceAll("[\\r\\n\\\\/\"]", "_");
            return new DownloadedFile(response.body(), contentType, fileName.isBlank() ? "comprobante" : fileName);
        } catch (DomainException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DomainException("payment_attachment_download_failed");
        }
    }
}
