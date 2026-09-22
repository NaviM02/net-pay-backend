package com.navi.net_pay_backend.infrastructure.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppServiceEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hash_id", nullable = false, unique = true, length = 50)
    private String hashId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "speed", nullable = false, length = 50)
    private String speed;

    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tp_type", referencedColumnName = "typology_id")
    private AdmTypologyEntity tpType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tp_status", referencedColumnName = "typology_id")
    private AdmTypologyEntity tpStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}