package com.navi.net_pay_backend.infrastructure.http.middleware;

import com.navi.net_pay_backend.infrastructure.http.controller.DataImportController;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Order(-1)
@RestControllerAdvice(assignableTypes = DataImportController.class)
public class ImportExceptionMiddleware {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> oversized(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body("import_file_too_large");
    }

    @ExceptionHandler({MultipartException.class, MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<String> invalidRequest(Exception ex) {
        return ResponseEntity.badRequest().body("import_invalid_request");
    }
}
