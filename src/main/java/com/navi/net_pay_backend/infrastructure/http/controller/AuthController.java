package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.model.LoginRequest;
import com.navi.net_pay_backend.application.model.LoginResponse;
import com.navi.net_pay_backend.application.use_case.LoginUseCase;
import com.navi.net_pay_backend.application.use_case.TwoFactorUseCase;
import com.navi.net_pay_backend.infrastructure.http.dto.LoginResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.TwoFactorVerifyRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final TwoFactorUseCase twoFactorUseCase;

    public AuthController(LoginUseCase loginUseCase, TwoFactorUseCase twoFactorUseCase) {
        this.loginUseCase = loginUseCase;
        this.twoFactorUseCase = twoFactorUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("User-Agent");
        if (userAgent == null) userAgent = "Spring";

        return loginResponse(loginUseCase.doLogin(request, userAgent));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<LoginResponseDto> verify(@RequestBody TwoFactorVerifyRequestDto request) {
        return loginResponse(twoFactorUseCase.verify(request.toRequest()));
    }

    private ResponseEntity<LoginResponseDto> loginResponse(LoginResponse response) {
        var builder = ResponseEntity.ok();
        if (response.getAuthc() != null) builder.header("Authorization", "Bearer " + response.getAuthc());
        return builder.body(LoginResponseDto.from(response));
    }
}
