package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.model.ForgotPasswordRequest;
import com.navi.net_pay_backend.application.model.ResetPasswordRequest;
import com.navi.net_pay_backend.application.use_case.AppClientUseCase;
import com.navi.net_pay_backend.application.use_case.AppUserUseCase;
import com.navi.net_pay_backend.application.use_case.AuditLogUseCase;
import com.navi.net_pay_backend.application.use_case.PasswordResetUseCase;
import com.navi.net_pay_backend.domain.dto.AppClientQueryDto;
import com.navi.net_pay_backend.domain.dto.AppUserQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.infrastructure.http.dto.AppClientResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.AppUserResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.AuditLogResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.MessageResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.ResetTokenValidationDto;
import com.navi.net_pay_backend.infrastructure.http.mapper.HttpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserUseCase userUseCase;
    private final AppClientUseCase clientUseCase;
    private final AuditLogUseCase auditLogUseCase;
    private final PasswordResetUseCase passwordResetUseCase;
    private final HttpMapper httpMapper;

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDto> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetUseCase.requestReset(request);
        return ResponseEntity.ok(MessageResponseDto.builder()
                .message("Si el correo está registrado, te enviamos un enlace para restablecer la contraseña.")
                .build());
    }

    @GetMapping("/reset-password/validate")
    public ResponseEntity<ResetTokenValidationDto> validateResetToken(@RequestParam String token) {
        return ResponseEntity.ok(ResetTokenValidationDto.builder()
                .valid(passwordResetUseCase.validateToken(token))
                .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDto> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetUseCase.resetPassword(request);
        return ResponseEntity.ok(MessageResponseDto.builder()
                .message("Contraseña actualizada correctamente.")
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<AppUserResponseDto> findMe() {
        return ResponseEntity.ok(httpMapper.toAppUserResponse(userUseCase.findMe()));
    }

    @GetMapping("/{hashId}/clients")
    public ResponseEntity<List<AppClientResponseDto>> findClients(
            @PathVariable String hashId,
            @ModelAttribute AppClientQueryDto queryDto
    ) {
        PaginatedResult<AppClient> result = clientUseCase.findByCollectorHashId(hashId, queryDto);
        List<AppClientResponseDto> clientRes = result.getItems().stream()
                .map(httpMapper::toAppClientResponse)
                .toList();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(result.getTotal())).body(clientRes);
    }

    @GetMapping("/{hashId}/audit-logs")
    public ResponseEntity<List<AuditLogResponseDto>> findAuditLogs(@PathVariable String hashId) {
        List<AuditLogResponseDto> response = auditLogUseCase.findByUserHashId(hashId).stream()
                .map(httpMapper::toAuditLogResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hashId}")
    public ResponseEntity<AppUserResponseDto> findByHashId(@PathVariable String hashId) {
        return ResponseEntity.ok(httpMapper.toAppUserResponse(userUseCase.findByHashId(hashId)));
    }

    @GetMapping
    public ResponseEntity<List<AppUserResponseDto>> findAll(@ModelAttribute AppUserQueryDto queryDto) {
        PaginatedResult<AppUser> result = userUseCase.findAll(queryDto);
        List<AppUserResponseDto> userRes = result.getItems().stream().map(httpMapper::toAppUserResponse).toList();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(result.getTotal())).body(userRes);
    }

    @PostMapping
    public ResponseEntity<AppUserResponseDto> create(@RequestBody AppUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(httpMapper.toAppUserResponse(userUseCase.create(user)));
    }

    @PutMapping("/{hashId}")
    public ResponseEntity<AppUserResponseDto> update(@PathVariable String hashId, @RequestBody AppUser user) {
        return ResponseEntity.ok(httpMapper.toAppUserResponse(userUseCase.update(hashId, user)));
    }

    @DeleteMapping("/{hashId}")
    public ResponseEntity<Void> delete(@PathVariable String hashId) {
        userUseCase.delete(hashId);
        return ResponseEntity.noContent().build();
    }
}
