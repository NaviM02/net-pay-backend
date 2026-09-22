package com.navi.net_pay_backend.infrastructure.service;

import com.navi.net_pay_backend.domain.service.ReceiptNumberGenerator;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class SequenceReceiptNumberGenerator implements ReceiptNumberGenerator {

    private static final String PREFIX = "REC";
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    void ensureSequence() {
        jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS receipt_number_seq START 1");
    }

    @Override
    public String next() {
        Long value = jdbcTemplate.queryForObject("SELECT nextval('receipt_number_seq')", Long.class);
        long number = value != null ? value : 1L;
        return String.format("%s-%d-%06d", PREFIX, Year.now().getValue(), number);
    }
}
