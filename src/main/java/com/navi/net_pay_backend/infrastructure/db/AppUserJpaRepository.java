package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.infrastructure.db.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserJpaRepository extends JpaRepository<AppUserEntity, Long> {
    Optional<AppUserEntity> findByEmailAndStatusId(String email, Long statusId);
}