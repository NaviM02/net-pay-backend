package com.navi.net_pay_backend.infrastructure.http.mapper;

import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.AppPayment;
import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.infrastructure.http.dto.AppClientResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.AppPaymentResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.AppServiceResponseDto;
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
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    public AppServiceResponseDto toAppServiceResponse(AppService service) {
        return AppServiceResponseDto.builder()
                .id(service.getId())
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

    public AppClientResponseDto toAppClientResponse(AppClient client) {
        return AppClientResponseDto.builder()
                .id(client.getId())
                .hashId(client.getHashId())
                .businessName(client.getBusinessName())
                .contactName(client.getContactName())
                .contactEmail(client.getContactEmail())
                .contactPhone(client.getContactPhone())
                .address(client.getAddress())
                .latitude(client.getLatitude())
                .longitude(client.getLongitude())
                .customerSince(client.getCustomerSince())
                .tpStatus(client.getTpStatus())
                .service(client.getService() != null ? toAppServiceResponse(client.getService()) : null)
                .collector(client.getCollector() != null ? toAppUserResponse(client.getCollector()) : null)
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }

    public AppPaymentResponseDto toAppPaymentResponse(AppPayment payment) {
        return AppPaymentResponseDto.builder()
                .id(payment.getId())
                .hashId(payment.getHashId())
                .client(payment.getClient() != null ? toAppClientResponse(payment.getClient()) : null)
                .service(payment.getService() != null ? toAppServiceResponse(payment.getService()) : null)
                .amount(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .collector(payment.getCollector() != null ? toAppUserResponse(payment.getCollector()) : null)
                .tpPaymentMethod(payment.getTpPaymentMethod())
                .tpPaymentStatus(payment.getTpPaymentStatus())
                .comment(payment.getComment())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
