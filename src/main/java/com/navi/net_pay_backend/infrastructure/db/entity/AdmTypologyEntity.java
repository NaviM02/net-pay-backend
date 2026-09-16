package com.navi.net_pay_backend.infrastructure.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "adm_typology")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdmTypologyEntity {
    @Id
    @Column(name = "typology_id")
    private Long typologyId;

    @Column(name = "internal_id", nullable = false, unique = true)
    private Long internalId;

    @Column(name = "parent_typology_id")
    private Long parentTypologyId;

    @Column(nullable = false, length = 150)
    private String description;

    @Column(name = "value_1", length = 255)
    private String value1;

    @Column(name = "value_2", length = 255)
    private String value2;
}
