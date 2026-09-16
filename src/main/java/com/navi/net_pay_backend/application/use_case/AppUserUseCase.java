package com.navi.net_pay_backend.application.use_case;


import com.navi.net_pay_backend.domain.dto.AppUserQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.exception.EntityAlreadyExistsException;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.service.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserUseCase {
    private final AppUserRepository userRepository;
    private final SecurityContext securityContext;

    public AppUser findMe() {
        Long userId = securityContext.getUserId().orElseThrow(() -> new DomainException("unauthorized"));
        return userRepository.findByIdAndStatusNot(userId, TypologyEnum.DELETED.getId()).orElseThrow(() -> new DomainException("user_not_found"));
    }

    public AppUser findByHashId(String hashId) {
        return userRepository.findByPublicIdAndStatusNot(hashId, TypologyEnum.DELETED.getId()).orElseThrow(() -> new DomainException("user_not_found"));
    }

    public PaginatedResult<AppUser> findAll(AppUserQueryDto queryDto) {
        return userRepository.findAll(queryDto);
    }

    public AppUser create(AppUser user) {
        if (userRepository.existsByEmailAndStatusIn(user.getEmail(), List.of(TypologyEnum.ACTIVE.getId(), TypologyEnum.INACTIVE.getId()))) {
            throw new EntityAlreadyExistsException("user_email_must_be_unique");
        }

        Long roleId = (user.getTpRole() != null && user.getTpRole().getTypologyId() != null)
                ? user.getTpRole().getTypologyId()
                : 2L;

        AppUser newUser = AppUser.builder()
                .hashId(UUID.randomUUID().toString())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .tpRole(AdmTypology.builder().typologyId(roleId).build())
                .tpStatus(AdmTypology.builder().typologyId(TypologyEnum.ACTIVE.getId()).build())
                .build();

        return userRepository.save(newUser);
    }

    public AppUser update(String hashId, AppUser userDetails) {
        AppUser existingUser = userRepository.findByPublicIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new DomainException("user_not_found"));

        AppUser updatedUser = AppUser.builder()
                .id(existingUser.getId())
                .hashId(existingUser.getHashId())
                .fullName(userDetails.getFullName())
                .email(userDetails.getEmail())
                .passwordHash(userDetails.getPasswordHash() != null ? userDetails.getPasswordHash() : existingUser.getPasswordHash())
                .tpRole(userDetails.getTpRole() != null ? userDetails.getTpRole() : existingUser.getTpRole())
                .tpStatus(userDetails.getTpStatus() != null ? userDetails.getTpStatus() : existingUser.getTpStatus())
                .lastLoginAt(existingUser.getLastLoginAt())
                .build();

        return userRepository.save(updatedUser);
    }

    public void deleteLogically(String hashId) {
        AppUser user = userRepository.findByPublicIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new DomainException("user_not_found"));

        AppUser deletedUser = AppUser.builder()
                .id(user.getId())
                .hashId(user.getHashId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .tpRole(user.getTpRole())
                .tpStatus(AdmTypology.builder().typologyId(TypologyEnum.DELETED.getId()).build())
                .lastLoginAt(user.getLastLoginAt())
                .build();

        userRepository.save(deletedUser);
    }
}
