package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.model.LoginRequest;
import com.navi.net_pay_backend.application.model.LoginResponse;
import com.navi.net_pay_backend.application.use_case.LoginUseCase;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("User-Agent");
        if (userAgent == null) userAgent = "Spring";

        String token = loginUseCase.doLogin(request, userAgent);
        return ResponseEntity.ok().header("Authorization", "Bearer " + token).body(new LoginResponse(token));
    }
}