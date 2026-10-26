package com.navi.net_pay_backend.application.model;

import com.navi.net_pay_backend.domain.entity.AdmTypology;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class AppServiceRequest {
    private String name;
    private String speed;
    private BigDecimal monthlyPrice;
    private AdmTypology tpType;
    private AdmTypology tpStatus;
}
