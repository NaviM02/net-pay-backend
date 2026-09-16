package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.entity.TokenCredential;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.repository.TokenCredentialRepository;
import com.navi.net_pay_backend.domain.service.model.TokenPayload;
import com.navi.net_pay_backend.domain.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TokenCredentialUseCase {

    private final TokenService tokenService;
    private final TokenCredentialRepository tokenCredentialRepository;

    @Value("${security.jwt.expiration}")
    private long expiresIn;

    public String create(AppUser user, String fingerprint, String userAgent) {
        String token = tokenService.generateToken(user.getEmail(), user.getId(), user.getTpRole().getInternalId());
        LocalDateTime effectiveDate = LocalDateTime.now();
        LocalDateTime expirationDate = effectiveDate.plusSeconds(expiresIn);

        TokenCredential credential = TokenCredential.builder()
                .userId(user.getId())
                .token(token)
                .effectiveDate(effectiveDate)
                .expirationDate(expirationDate)
                .userAgent(userAgent)
                .fingerprint(fingerprint)
                .build();

        tokenCredentialRepository.deleteByUserIdAndFingerprint(user.getId(), fingerprint);

        tokenCredentialRepository.save(credential);

        return token;
    }

    public TokenPayload verify(String token) {
        TokenPayload payload = tokenService.verifyToken(token);

        if (payload == null) return null;
        boolean valid = tokenCredentialRepository.existsByTokenAndExpirationDate(token, LocalDateTime.now());

        if (!valid) return null;
        return payload;
    }

    public TokenCredential findLastTokenByUserId(Long userId) {
        return tokenCredentialRepository.findLastTokenByUserId(userId).orElse(null);
    }
}