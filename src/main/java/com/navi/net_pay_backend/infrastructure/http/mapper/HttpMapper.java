package com.navi.net_pay_backend.infrastructure.http.mapper;

import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.infrastructure.http.dto.AppUserResponseDto;
import org.springframework.stereotype.Service;

@Service
public class HttpMapper {

    public AppUserResponseDto toAppUserResponse(AppUser user) {
        return AppUserResponseDto.builder()
                .id(user.getId())
                .hashId(user.getHashId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .tpRole(user.getTpRole())
                .tpStatus(user.getTpStatus())
                .build();
    }
}
