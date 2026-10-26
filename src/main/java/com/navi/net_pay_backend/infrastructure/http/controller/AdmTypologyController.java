package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.AdmTypologyUseCase;
import com.navi.net_pay_backend.domain.entity.AdmTypology;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/typologies")
@RequiredArgsConstructor
public class AdmTypologyController {

    private final AdmTypologyUseCase typologyUseCase;

    @GetMapping("/{internalId}")
    public ResponseEntity<AdmTypology> findByInternalId(
            @PathVariable Long internalId
    ) {
        return ResponseEntity.ok(
                typologyUseCase.findByInternalId(internalId)
        );
    }

    @GetMapping("/by-parent/{parentInternalId}")
    public ResponseEntity<List<AdmTypology>> findByParentInternalId(
            @PathVariable Long parentInternalId
    ) {
        return ResponseEntity.ok(
                typologyUseCase.findByParentInternalId(parentInternalId)
        );
    }
}