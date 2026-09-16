package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.service.PasswordHashService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class BcryptHashService implements PasswordHashService {

    private final PasswordEncoder passwordEncoder;

    public BcryptHashService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String data) {
        return passwordEncoder.encode(data);
    }

    @Override
    public boolean compare(String data, String hashedData) {
        return passwordEncoder.matches(data, hashedData);
    }
}