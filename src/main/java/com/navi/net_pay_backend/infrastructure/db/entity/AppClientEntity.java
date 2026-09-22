package com.navi.net_pay_backend.infrastructure.db.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppClientEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hash_id", nullable = false, unique = true, length = 50)
    private String hashId;

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "contact_name", nullable = false, length = 150)
    private String contactName;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "customer_since")
    private LocalDate customerSince;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", referencedColumnName = "id", nullable = false)
    private AppServiceEntity service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id", referencedColumnName = "id")
    private AppUserEntity collector;

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
