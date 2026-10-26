package com.navi.net_pay_backend.infrastructure.db.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_challenge")
@Getter
@Setter
@NoArgsConstructor
public class LoginChallengeEntity {
    @Id
    @Column(name = "hash_id", length = 50)
    private String hashId;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private int attempts;
    @Column(nullable = false)
    private boolean consumed;
    @Column(columnDefinition = "TEXT")
    private String fingerprint;
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    @Column(nullable = false, length = 150)
    private String email;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
}
