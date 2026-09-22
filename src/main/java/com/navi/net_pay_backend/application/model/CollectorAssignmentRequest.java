package com.navi.net_pay_backend.application.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class CollectorAssignmentRequest {
    private Long collectorId;
}
