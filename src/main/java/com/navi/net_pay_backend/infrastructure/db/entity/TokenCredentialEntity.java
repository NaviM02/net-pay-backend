package com.navi.net_pay_backend.infrastructure.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_credential")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenCredentialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true, length = 750)
    private String token;

    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "user_agent", length = 250)
    private String userAgent;

    @Column(length = 500)
    private String fingerprint;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}