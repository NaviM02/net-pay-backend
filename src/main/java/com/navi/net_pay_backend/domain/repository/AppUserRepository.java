package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.dto.AppUserQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppUser;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository {
    Optional<AppUser> findByEmailAndStatus(String email, Long statusId);
    void updateLastLogin(Long userId);

    Optional<AppUser> findByIdAndStatusNot(Long id, Long statusId);

    Optional<AppUser> findByPublicIdAndStatusNot(String publicId, Long statusId);

    Optional<AppUser> findByResetTokenHash(String resetTokenHash);

    PaginatedResult<AppUser> findAll(AppUserQueryDto queryDto);

    boolean existsByEmailAndStatusIn(String email, List<Long> statusIds);

    boolean existsByEmailAndStatusNot(String email, Long statusId);

    AppUser save(AppUser user);
}