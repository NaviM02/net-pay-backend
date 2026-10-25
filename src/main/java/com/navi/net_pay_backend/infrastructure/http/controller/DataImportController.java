package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.DataImportUseCase;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.infrastructure.http.dto.ImportResponseDto;
import com.navi.net_pay_backend.infrastructure.service.SpreadsheetImportReader;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
public class DataImportController {
    private final SpreadsheetImportReader reader;
    private final DataImportUseCase useCase;

    @PostMapping(value = "/{resource}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResponseDto> upload(@PathVariable String resource,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "false") boolean dryRun,
            @RequestParam(defaultValue = ",") String delimiter) {
        if (file.isEmpty()) throw new DomainException("import_empty_file");
        if (file.getSize() > 5 * 1024 * 1024) throw new DomainException("import_file_too_large");
        if (delimiter.length() != 1) throw new DomainException("import_invalid_delimiter");
        try (var input = file.getInputStream()) {
            var result = useCase.execute(resource, reader.read(input, file.getOriginalFilename(), delimiter.charAt(0)), dryRun);
            return ResponseEntity.ok(new ImportResponseDto(result.dryRun(), result.total(), result.successful(),
                    result.failed(), result.rows().stream().map(r -> new ImportResponseDto.RowResult(
                            r.row(), r.status(), r.hashId(), r.error())).toList()));
        } catch (IOException ex) {
            throw new DomainException("import_file_read_error");
        }
    }
}
