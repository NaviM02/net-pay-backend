package com.navi.net_pay_backend.infrastructure.http.controller;

import com.navi.net_pay_backend.application.use_case.AppUserUseCase;
import com.navi.net_pay_backend.domain.dto.AppUserQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.infrastructure.http.dto.AppUserResponseDto;
import com.navi.net_pay_backend.infrastructure.http.mapper.HttpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AppUserController {

    private final AppUserUseCase userUseCase;
    private final HttpMapper httpMapper;

    @GetMapping("/me")
    public ResponseEntity<AppUser> findMe() {
        return ResponseEntity.ok(userUseCase.findMe());
    }

    @GetMapping("/{hashId}")
    public ResponseEntity<AppUser> findByHashId(@PathVariable String hashId) {
        return ResponseEntity.ok(userUseCase.findByHashId(hashId));
    }

    @GetMapping
    public ResponseEntity<List<AppUserResponseDto>> findAll(@ModelAttribute AppUserQueryDto queryDto) {
        PaginatedResult<AppUser> result = userUseCase.findAll(queryDto);
        List<AppUserResponseDto> userRes = result.getItems().stream().map(httpMapper::toAppUserResponse).toList();
        return ResponseEntity.ok().header("X-Total-Count", String.valueOf(result.getTotal())).body(userRes);
    }

    @PostMapping
    public ResponseEntity<AppUser> create(@RequestBody AppUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userUseCase.create(user));
    }

    @PutMapping("/{hashId}")
    public ResponseEntity<AppUser> update(@PathVariable String hashId, @RequestBody AppUser user) {
        return ResponseEntity.ok(userUseCase.update(hashId, user));
    }

    @DeleteMapping("/{hashId}")
    public ResponseEntity<Void> delete(@PathVariable String hashId) {
        userUseCase.deleteLogically(hashId);
        return ResponseEntity.noContent().build();
    }
}
