package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.application.model.ChangePasswordRequest;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.exception.InvalidCredentialsException;
import com.navi.net_pay_backend.domain.exception.UnauthorizeException;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.service.AuditLogService;
import com.navi.net_pay_backend.domain.service.PasswordHashService;
import com.navi.net_pay_backend.domain.service.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {
    private final AppUserRepository userRepository;
    private final PasswordHashService passwordHashService;
    private final SecurityContext securityContext;
    private final AuditLogService auditLogService;

    @Transactional
    public void changeOwnPassword(ChangePasswordRequest request) {
        AppUser actor = authenticate(request);
        changePassword(actor, request);
    }

    @Transactional
    public void changeUserPassword(String hashId, ChangePasswordRequest request) {
        authenticate(request);
        AppUser target = userRepository.findByPublicIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("user_not_found"));
        changePassword(target, request);
    }

    private AppUser authenticate(ChangePasswordRequest request) {
        Long actorId = securityContext.getUserId().orElseThrow(() -> new UnauthorizeException("unauthorized"));
        AppUser actor = userRepository.findByIdAndStatusNot(actorId, TypologyEnum.DELETED.getId())
                .filter(user -> user.getTpStatus() != null
                        && TypologyEnum.ACTIVE.getId().equals(user.getTpStatus().getInternalId()))
                .orElseThrow(() -> new UnauthorizeException("unauthorized"));
        if (request == null || request.currentPassword() == null || request.currentPassword().isBlank()) {
            throw new DomainException("current_password_required");
        }
        if (!passwordHashService.compare(request.currentPassword(), actor.getPassword())) {
            throw new InvalidCredentialsException("wrong_password");
        }
        if (request.newPassword() == null || request.newPassword().isBlank()) {
            throw new DomainException("password_required");
        }
        if (request.newPassword().length() < 8) {
            throw new DomainException("password_too_short");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new DomainException("password_confirmation_mismatch");
        }
        return actor;
    }

    private void changePassword(AppUser target, ChangePasswordRequest request) {
        userRepository.save(target.toBuilder()
                .password(passwordHashService.hash(request.newPassword()))
                .resetTokenHash(null)
                .resetTokenExpiresAt(null)
                .build());
        auditLogService.record("user_password_changed", "Cambió contraseña de usuario",
                "USER", target.getHashId(), Map.of());
    }
}
