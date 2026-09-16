package com.navi.net_pay_backend.domain.service;

import com.navi.net_pay_backend.domain.service.model.SecurityPrincipal;

import java.util.Optional;

public interface SecurityContext {
    Optional<Long> getUserId();

    void setContext(SecurityPrincipal principal);

    void clear();
}