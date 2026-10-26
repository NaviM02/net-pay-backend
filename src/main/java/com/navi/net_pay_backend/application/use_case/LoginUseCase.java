package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.application.model.LoginRequest;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.InvalidCredentialsException;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.service.PasswordHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final AppUserRepository appUserRepository;
    private final PasswordHashService passwordHashService;
    private final TokenCredentialUseCase tokenCredentialUseCase;

    public String doLogin(LoginRequest request, String userAgent) {
        AppUser user = appUserRepository
                .findByEmailAndStatus(request.getEmail(), TypologyEnum.ACTIVE.getId())
                .orElseThrow(() -> new InvalidCredentialsException("invalid_email"));

        if (!passwordHashService.compare(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("wrong_password");
        }

        appUserRepository.updateLastLogin(user.getId());

        return tokenCredentialUseCase.create(user, request.getFingerprint(), userAgent);
    }
}