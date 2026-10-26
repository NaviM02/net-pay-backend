package com.navi.net_pay_backend.domain.service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoredFile {
    private final String url;
    private final String publicId;
}
