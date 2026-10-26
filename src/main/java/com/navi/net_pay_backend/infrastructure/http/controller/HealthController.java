package com.navi.net_pay_backend.infrastructure.http.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Minimal readiness check: no database, version or configuration details are exposed. */
@RestController
@RequiredArgsConstructor
public class HealthController {
    private final JdbcTemplate jdbc;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok(Map.of("status", "UP"));
        } catch (DataAccessException exception) {
            return ResponseEntity.status(503).body(Map.of("status", "DOWN"));
        }
    }
}
