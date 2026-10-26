package com.navi.net_pay_backend.domain.service.model;

public record DownloadedFile(byte[] content, String contentType, String fileName) {
}
