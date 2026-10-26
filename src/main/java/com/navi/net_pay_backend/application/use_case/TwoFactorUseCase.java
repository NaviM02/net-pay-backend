package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.application.model.LoginResponse;
import com.navi.net_pay_backend.application.model.TwoFactorVerifyRequest;
import com.navi.net_pay_backend.domain.dto.EmailMessage;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.entity.LoginChallenge;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.InvalidCredentialsException;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.repository.LoginChallengeRepository;
import com.navi.net_pay_backend.domain.service.EmailSender;
import com.navi.net_pay_backend.domain.service.EmailTemplateRenderer;
import com.navi.net_pay_backend.domain.service.PasswordHashService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TwoFactorUseCase {
    private static final int MAX_ATTEMPTS = 5;
    private final SecureRandom random = new SecureRandom();
    private final LoginChallengeRepository challenges;
    private final AppUserRepository users;
    private final PasswordHashService passwords;
    private final EmailSender emailSender;
    private final EmailTemplateRenderer templates;
    private final TokenCredentialUseCase tokens;

    @Value("${security.two-factor.expiration-minutes:5}")
    private int expirationMinutes;

    @Transactional
    public LoginResponse begin(AppUser user, String fingerprint, String userAgent) {
        challenges.lockUser(user.getId());
        LocalDateTime now = LocalDateTime.now();
        if (challenges.latestCreatedAt(user.getId()).filter(created -> created.isAfter(now.minusSeconds(60))).isPresent()) {
            throw new DomainException("two_factor_retry_later");
        }
        String code = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
        LocalDateTime expiresAt = now.plusMinutes(expirationMinutes);
        LoginChallenge challenge = LoginChallenge.builder()
                .hashId(UUID.randomUUID().toString()).userId(user.getId()).codeHash(passwords.hash(code))
                .expiresAt(expiresAt).createdAt(now).fingerprint(fingerprint).userAgent(userAgent)
                .email(user.getEmail()).passwordHash(user.getPassword()).build();
        challenges.save(challenge);
        emailSender.send(EmailMessage.builder().to(user.getEmail())
                .subject("Código de inicio de sesión · NetPay Manager")
                .htmlBody(templates.render("Verificación en dos pasos",
                        "<p>Tu código de inicio de sesión es:</p><h2>" + code + "</h2><p>Vence en "
                                + expirationMinutes + " minutos. Si no solicitaste este código, ignora este correo.</p>"))
                .build());
        return new LoginResponse(null, true, challenge.getHashId(), expiresAt);
    }

    // Failed attempts must commit, while other failures roll back token creation and challenge consumption.
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public LoginResponse verify(TwoFactorVerifyRequest request) {
        if (request == null || request.challengeId() == null || request.challengeId().isBlank()) {
            throw invalid();
        }
        LoginChallenge challenge = challenges.findForUpdate(request.challengeId()).orElseThrow(this::invalid);
        if (challenge.isConsumed() || challenge.getAttempts() >= MAX_ATTEMPTS
                || !challenge.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw invalid();
        }
        AppUser user = users.findByIdAndStatusNot(challenge.getUserId(), TypologyEnum.DELETED.getId())
                .filter(candidate -> candidate.getTpStatus() != null
                        && TypologyEnum.ACTIVE.getId().equals(candidate.getTpStatus().getInternalId())
                        && Boolean.TRUE.equals(candidate.getTwoFactorEnabled())
                        && challenge.getEmail().equals(candidate.getEmail())
                        && challenge.getPasswordHash().equals(candidate.getPassword()))
                .orElseThrow(this::invalid);
        String code = request.code();
        if (code == null || !code.matches("[0-9]{6}") || !passwords.compare(code, challenge.getCodeHash())) {
            challenges.save(challenge.toBuilder().attempts(challenge.getAttempts() + 1).build());
            throw invalid();
        }
        challenges.save(challenge.toBuilder().consumed(true).build());
        String token = tokens.create(user, challenge.getFingerprint(), challenge.getUserAgent());
        users.updateLastLogin(user.getId());
        return new LoginResponse(token);
    }

    private InvalidCredentialsException invalid() {
        return new InvalidCredentialsException("invalid_two_factor_code");
    }
}
