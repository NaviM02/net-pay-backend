package com.navi.net_pay_backend.infrastructure.http.mapper;

import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.infrastructure.http.dto.AppServiceResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.AppUserResponseDto;
import org.springframework.stereotype.Service;

@Service
public class HttpMapper {

    public AppUserResponseDto toAppUserResponse(AppUser user) {
        return AppUserResponseDto.builder()
                .hashId(user.getHashId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .tpRole(user.getTpRole())
                .tpStatus(user.getTpStatus())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    public AppServiceResponseDto toAppServiceResponse(AppService service) {
        return AppServiceResponseDto.builder()
                .hashId(service.getHashId())
                .name(service.getName())
                .speed(service.getSpeed())
                .monthlyPrice(service.getMonthlyPrice())
                .clientCount(service.getClientCount())
                .tpType(service.getTpType())
                .tpStatus(service.getTpStatus())
                .createdAt(service.getCreatedAt())
                .updatedAt(service.getUpdatedAt())
                .build();
    }
}
