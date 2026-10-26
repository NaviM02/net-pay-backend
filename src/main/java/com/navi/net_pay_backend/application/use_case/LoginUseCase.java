package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.application.model.LoginRequest;
import com.navi.net_pay_backend.application.model.LoginResponse;
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
    private final TwoFactorUseCase twoFactorUseCase;

    public LoginResponse doLogin(LoginRequest request, String userAgent) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidCredentialsException("invalid_credentials");
        }
        AppUser user = appUserRepository
                .findByEmailAndStatus(request.getEmail(), TypologyEnum.ACTIVE.getId())
                .orElseThrow(() -> new InvalidCredentialsException("invalid_email"));

        if (!passwordHashService.compare(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("wrong_password");
        }

        if (Boolean.TRUE.equals(user.getTwoFactorEnabled())) {
            return twoFactorUseCase.begin(user, request.getFingerprint(), userAgent);
        }

        String token = tokenCredentialUseCase.create(user, request.getFingerprint(), userAgent);
        appUserRepository.updateLastLogin(user.getId());
        return new LoginResponse(token);
    }
}
