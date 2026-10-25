package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.AccessControlUseCase;
import com.navi.net_pay_backend.domain.dto.RoleQueryDto;
import com.navi.net_pay_backend.infrastructure.http.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccessControlController {
    private final AccessControlUseCase access;

    @GetMapping("/permissions")
    public ResponseEntity<List<PermissionResponseDto>> permissions() {
        return ResponseEntity.ok(access.permissions().stream().map(PermissionResponseDto::from).toList());
    }
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponseDto>> roles(@ModelAttribute RoleQueryDto query) {
        var result = access.roles(query);
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(result.getTotal()))
                .body(result.getItems().stream().map(RoleResponseDto::from).toList());
    }
    @GetMapping("/roles/{hashId}")
    public ResponseEntity<RoleResponseDto> role(@PathVariable String hashId) {
        return ResponseEntity.ok(RoleResponseDto.from(access.role(hashId)));
    }
    @PostMapping("/roles")
    public ResponseEntity<RoleResponseDto> create(@RequestBody RoleRequestDto request) {
        return ResponseEntity.status(201).body(RoleResponseDto.from(access.saveRole(null, request.toRequest())));
    }
    @PutMapping("/roles/{hashId}")
    public ResponseEntity<RoleResponseDto> update(@PathVariable String hashId, @RequestBody RoleRequestDto request) {
        return ResponseEntity.ok(RoleResponseDto.from(access.saveRole(hashId, request.toRequest())));
    }
    @DeleteMapping("/roles/{hashId}")
    public ResponseEntity<Void> delete(@PathVariable String hashId) {
        access.deleteRole(hashId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/users/{userHashId}/roles")
    public ResponseEntity<List<UserRoleResponseDto>> assignments(@PathVariable String userHashId) {
        return ResponseEntity.ok(access.assignments(userHashId).stream().map(UserRoleResponseDto::from).toList());
    }
    @PostMapping("/users/{userHashId}/roles")
    public ResponseEntity<UserRoleResponseDto> assign(@PathVariable String userHashId, @RequestBody UserRoleRequestDto request) {
        return ResponseEntity.status(201).body(UserRoleResponseDto.from(access.assign(userHashId, request.toRequest())));
    }
    @DeleteMapping("/users/{userHashId}/roles/{assignmentHashId}")
    public ResponseEntity<Void> cancel(@PathVariable String userHashId, @PathVariable String assignmentHashId) {
        access.cancel(userHashId, assignmentHashId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/auth/access")
    public ResponseEntity<AccessResponseDto> myAccess() {
        return ResponseEntity.ok(new AccessResponseDto(access.myAssignments().stream().map(UserRoleResponseDto::from).toList(),
                access.myPermissions()));
    }
}
