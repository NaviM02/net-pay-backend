package com.navi.net_pay_backend.application.model;

import com.navi.net_pay_backend.domain.entity.AdmTypology;
import com.navi.net_pay_backend.domain.entity.AppUser;

public record AppUserRequest(String fullName, String email, String password, Long status, Boolean twoFactorEnabled) {
    public AppUser toUser() {
        return AppUser.builder().fullName(fullName).email(email).password(password)
                .twoFactorEnabled(twoFactorEnabled)
                .tpStatus(status == null ? null : AdmTypology.builder().internalId(status).build()).build();
    }
}
