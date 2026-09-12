package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.entity.AppUser;

import java.util.Optional;

public interface AppUserRepository {
    Optional<AppUser> findByEmailAndStatus(String email, Long statusId);
    void updateLastLogin(Long userId);
}