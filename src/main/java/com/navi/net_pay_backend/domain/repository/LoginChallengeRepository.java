package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.entity.LoginChallenge;
import java.util.Optional;
import java.time.LocalDateTime;

public interface LoginChallengeRepository {
    void save(LoginChallenge challenge);
    Optional<LoginChallenge> findForUpdate(String hashId);
    void lockUser(Long userId);
    Optional<LocalDateTime> latestCreatedAt(Long userId);
}
