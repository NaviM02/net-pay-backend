package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.application.model.ForgotPasswordRequest;
import com.navi.net_pay_backend.application.model.ResetPasswordRequest;
import com.navi.net_pay_backend.domain.dto.EmailMessage;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.service.AuditLogService;
import com.navi.net_pay_backend.domain.service.EmailSender;
import com.navi.net_pay_backend.domain.service.EmailTemplateRenderer;
import com.navi.net_pay_backend.domain.service.PasswordHashService;
import com.navi.net_pay_backend.domain.service.PasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PasswordResetUseCase {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String RESET_PATH = "/reset-password";
    private static final String SUBJECT = "Recuperación de contraseña · NetPay Manager";

    private final AppUserRepository userRepository;
    private final PasswordHashService passwordHashService;
    private final PasswordResetTokenService tokenService;
    private final EmailTemplateRenderer templateRenderer;
    private final EmailSender emailSender;
    private final AuditLogService auditLogService;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    public void requestReset(ForgotPasswordRequest request) {
        if (request == null || isBlank(request.getEmail())) {
            throw new DomainException("email_required");
        }

        userRepository.findByEmailAndStatus(request.getEmail().trim(), TypologyEnum.ACTIVE.getId())
                .ifPresent(this::sendResetEmail);
    }

    public boolean validateToken(String token) {
        if (isBlank(token)) {
            return false;
        }
        return userRepository.findByResetTokenHash(tokenService.hashToken(token))
                .filter(user -> !isDeleted(user))
                .map(this::notExpired)
                .orElse(false);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        validateNewPassword(request);

        AppUser user = userRepository.findByResetTokenHash(tokenService.hashToken(request.getToken()))
                .filter(candidate -> !isDeleted(candidate))
                .orElseThrow(() -> new DomainException("invalid_reset_token"));

        if (!notExpired(user)) {
            throw new DomainException("reset_token_expired");
        }

        userRepository.save(user.toBuilder()
                .password(passwordHashService.hash(request.getNewPassword()))
                .resetTokenHash(null)
                .resetTokenExpiresAt(null)
                .build());

        auditLogService.record("password_reset_completed", "Restableció su contraseña",
                "USER", user.getHashId(), Map.of());
    }

    private void sendResetEmail(AppUser user) {
        String rawToken = tokenService.generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(tokenService.expiryMinutes());

        userRepository.save(user.toBuilder()
                .resetTokenHash(tokenService.hashToken(rawToken))
                .resetTokenExpiresAt(expiresAt)
                .build());

        String resetUrl = resolveFrontendBaseUrl() + RESET_PATH + "?token=" + rawToken;
        String html = templateRenderer.renderPasswordReset(
                user.getFullName(), resetUrl, tokenService.expiryMinutes());

        emailSender.send(EmailMessage.builder()
                .to(user.getEmail())
                .subject(SUBJECT)
                .htmlBody(html)
                .build());

        auditLogService.record("password_reset_requested", "Solicitó recuperación de contraseña",
                "USER", user.getHashId(), Map.of());
    }

    private void validateNewPassword(ResetPasswordRequest request) {
        if (request == null || isBlank(request.getToken())) {
            throw new DomainException("reset_token_required");
        }
        if (isBlank(request.getNewPassword())) {
            throw new DomainException("password_required");
        }
        if (request.getNewPassword().length() < MIN_PASSWORD_LENGTH) {
            throw new DomainException("password_too_short");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new DomainException("password_confirmation_mismatch");
        }
    }

    private boolean notExpired(AppUser user) {
        return user.getResetTokenExpiresAt() != null
                && user.getResetTokenExpiresAt().isAfter(LocalDateTime.now());
    }

    private boolean isDeleted(AppUser user) {
        return user.getTpStatus() != null
                && TypologyEnum.DELETED.getId().equals(user.getTpStatus().getInternalId());
    }

    private String resolveFrontendBaseUrl() {
        if (frontendBaseUrl == null) {
            return "";
        }
        String base = frontendBaseUrl.trim();
        // Defensa: en .properties las comillas son literales, no un delimitador
        if (base.length() >= 2 && base.startsWith("\"") && base.endsWith("\"")) {
            base = base.substring(1, base.length() - 1).trim();
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
