package com.navi.net_pay_backend.application.use_case;


import com.navi.net_pay_backend.domain.dto.AppUserQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.exception.EntityAlreadyExistsException;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.service.AuditLogService;
import com.navi.net_pay_backend.domain.service.PasswordHashService;
import com.navi.net_pay_backend.domain.service.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserUseCase {
    private final AppUserRepository userRepository;
    private final PasswordHashService passwordHashService;
    private final AuditLogService auditLogService;
    private final SecurityContext securityContext;

    public AppUser findMe() {
        Long userId = securityContext.getUserId().orElseThrow(() -> new DomainException("unauthorized"));
        return userRepository.findByIdAndStatusNot(userId, TypologyEnum.DELETED.getId()).orElseThrow(() -> new DomainException("user_not_found"));
    }

    public AppUser findByHashId(String hashId) {
        return userRepository.findByPublicIdAndStatusNot(hashId, TypologyEnum.DELETED.getId()).orElseThrow(() -> new DomainException("user_not_found"));
    }

    public PaginatedResult<AppUser> findAll(AppUserQueryDto queryDto) {
        queryDto.setExcludeStatus(TypologyEnum.DELETED.getId());
        return userRepository.findAll(queryDto);
    }

    @Transactional
    public AppUser create(AppUser user) {
        if (isBlank(user.getPassword())) {
            throw new DomainException("user_password_required");
        }
        if (userRepository.existsByEmailAndStatusNot(user.getEmail(), TypologyEnum.DELETED.getId())) {
            throw new EntityAlreadyExistsException("user_email_must_be_unique");
        }

        AppUser newUser = AppUser.builder()
                .hashId(UUID.randomUUID().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .password(passwordHashService.hash(user.getPassword()))
                .tpStatus(user.getTpStatus() == null ? AdmTypology.builder().internalId(TypologyEnum.ACTIVE.getId()).build() : user.getTpStatus())
                .build();

        AppUser saved = userRepository.save(newUser);
        auditLogService.record("user_created", "Creó usuario", "USER", saved.getHashId(), after(saved));
        return saved;
    }

    @Transactional
    public AppUser update(String hashId, AppUser userDetails) {
        if (!isBlank(userDetails.getPassword())) {
            throw new DomainException("use_change_password_endpoint");
        }
        AppUser existingUser = userRepository.findByPublicIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new DomainException("user_not_found"));

        String email = userDetails.getEmail() != null ? userDetails.getEmail() : existingUser.getEmail();
        if (!email.equals(existingUser.getEmail())
                && userRepository.existsByEmailAndStatusNot(email, TypologyEnum.DELETED.getId())) {
            throw new EntityAlreadyExistsException("user_email_must_be_unique");
        }

        AppUser updatedUser = AppUser.builder()
                .id(existingUser.getId())
                .hashId(existingUser.getHashId())
                .fullName(userDetails.getFullName())
                .email(email)
                .password(existingUser.getPassword())
                .tpStatus(userDetails.getTpStatus() != null ? userDetails.getTpStatus() : existingUser.getTpStatus())
                .lastLoginAt(existingUser.getLastLoginAt())
                .resetTokenHash(existingUser.getResetTokenHash())
                .resetTokenExpiresAt(existingUser.getResetTokenExpiresAt())
                .build();

        AppUser saved = userRepository.save(updatedUser);
        auditLogService.record("user_updated", "Modificó usuario", "USER", saved.getHashId(),
                diff(existingUser, saved));
        return saved;
    }

    @Transactional
    public void delete(String hashId) {
        AppUser user = userRepository.findByPublicIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new DomainException("user_not_found"));

        AppUser deletedUser = AppUser.builder()
                .id(user.getId())
                .hashId(user.getHashId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .password(user.getPassword())
                .tpStatus(AdmTypology.builder().internalId(TypologyEnum.DELETED.getId()).build())
                .lastLoginAt(user.getLastLoginAt())
                .build();

        userRepository.save(deletedUser);
        auditLogService.record("user_deleted", "Eliminó usuario", "USER", user.getHashId(), before(user));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Map<String, Object> snapshot(AppUser user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("fullName", user.getFullName());
        map.put("email", user.getEmail());
        map.put("status", user.getTpStatus() != null ? user.getTpStatus().getInternalId() : null);
        return map;
    }

    private Map<String, Object> before(AppUser user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("before", snapshot(user));
        return map;
    }

    private Map<String, Object> after(AppUser user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("after", snapshot(user));
        return map;
    }

    private Map<String, Object> diff(AppUser before, AppUser after) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("before", snapshot(before));
        map.put("after", snapshot(after));
        return map;
    }
}
