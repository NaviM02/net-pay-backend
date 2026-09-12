package com.navi.net_pay_backend.domain.entity;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AdmTypology {
    private Long typologyId;
    private Long internalId;
    private Long parentTypologyId;
    private String description;
    private String value1;
    private String value2;
}
