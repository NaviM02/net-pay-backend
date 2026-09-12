package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.infrastructure.db.entity.AppUserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DbUserAccountRepository implements AppUserRepository {

    private final AppUserJpaRepository repository;

    @Override
    public Optional<AppUser> findByEmailAndStatus(String email, Long statusId) {
        return repository.findByEmailAndStatusId(email, statusId).map(this::toDomain);
    }

    @Override
    public void updateLastLogin(Long userId) {
        AppUserEntity user = repository.findById(userId).orElseThrow();
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        repository.save(user);
    }

    private AppUser toDomain(AppUserEntity entity) {
        return AppUser.builder()
                .id(entity.getId())
                .publicId(entity.getHashId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .roleId(entity.getRoleId())
                .statusId(entity.getStatusId())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }
}