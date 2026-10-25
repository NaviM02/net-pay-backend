package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.FileUploadUseCase;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.infrastructure.http.dto.FileUploadResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {
    private final FileUploadUseCase fileUpload;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDto> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) throw new DomainException("file_required");
        if (file.getSize() > FileUploadUseCase.MAX_SIZE_BYTES) throw new DomainException("file_too_large");
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new FileUploadResponseDto(fileUpload.upload(file.getBytes())));
        } catch (IOException ex) {
            throw new DomainException("file_read_failed");
        }
    }
}
