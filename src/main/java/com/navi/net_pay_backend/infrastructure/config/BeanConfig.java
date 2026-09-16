package com.navi.net_pay_backend.infrastructure.config;

import com.navi.net_pay_backend.application.use_case.TokenCredentialUseCase;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.service.PasswordHashService;
import com.navi.net_pay_backend.application.use_case.LoginUseCase;
import com.navi.net_pay_backend.infrastructure.db.DbTokenCredentialRepository;
import com.navi.net_pay_backend.infrastructure.service.JwtTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
    @Bean
    public LoginUseCase loginUseCase(AppUserRepository userRepository, PasswordHashService hashService, TokenCredentialUseCase tokenCredentialUseCase) {
        return new LoginUseCase(userRepository, hashService, tokenCredentialUseCase);
    }

    @Bean
    public TokenCredentialUseCase tokenCredentialUseCase(JwtTokenService tokenService, DbTokenCredentialRepository tokenCredentialRepository) {
        return new  TokenCredentialUseCase(tokenService, tokenCredentialRepository);
    }
}
