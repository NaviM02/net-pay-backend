package com.navi.net_pay_backend.infrastructure.http.mapper;

import com.navi.net_pay_backend.domain.dto.ClientAccountView;
import com.navi.net_pay_backend.domain.dto.ClientChargeView;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.AppPayment;
import com.navi.net_pay_backend.domain.entity.AppService;
import com.navi.net_pay_backend.domain.entity.AppUser;
import com.navi.net_pay_backend.domain.entity.PaymentAllocation;
import com.navi.net_pay_backend.domain.entity.Receipt;
import com.navi.net_pay_backend.infrastructure.http.dto.AppClientResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.AppPaymentAllocationDto;
import com.navi.net_pay_backend.infrastructure.http.dto.AppPaymentResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.AppServiceResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.AppUserResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.ClientAccountResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.ClientChargeResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.ReceiptResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

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
                .reference(payment.getReference())
                .comment(payment.getComment())
                .allocations(payment.getAllocations() != null
                        ? payment.getAllocations().stream().map(this::toAppPaymentAllocation).toList()
                        : null)
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public AppPaymentAllocationDto toAppPaymentAllocation(PaymentAllocation allocation) {
        return AppPaymentAllocationDto.builder()
                .id(allocation.getId())
                .chargeHashId(allocation.getCharge() != null ? allocation.getCharge().getHashId() : null)
                .year(allocation.getCharge() != null && allocation.getCharge().getPeriod() != null
                        ? allocation.getCharge().getPeriod().getYear() : null)
                .month(allocation.getCharge() != null && allocation.getCharge().getPeriod() != null
                        ? allocation.getCharge().getPeriod().getMonth() : null)
                .periodLabel(allocation.getCharge() != null && allocation.getCharge().getPeriod() != null
                        ? allocation.getCharge().getPeriod().getLabel() : null)
                .amount(allocation.getAmount())
                .build();
    }

    public ReceiptResponseDto toReceiptResponse(Receipt receipt) {
        return ReceiptResponseDto.builder()
                .hashId(receipt.getHashId())
                .receiptNumber(receipt.getReceiptNumber())
                .paymentHashId(receipt.getPayment() != null ? receipt.getPayment().getHashId() : null)
                .clientHashId(receipt.getClient() != null ? receipt.getClient().getHashId() : null)
                .clientName(receipt.getClient() != null ? receipt.getClient().getBusinessName() : null)
                .periodLabel(receipt.getPeriod() != null ? receipt.getPeriod().getLabel() : null)
                .amount(receipt.getAmount())
                .issuedAt(receipt.getIssuedAt())
                .collectorName(receipt.getCollector() != null ? receipt.getCollector().getFullName() : null)
                .documentPath(receipt.getDocumentPath())
                .printedAt(receipt.getPrintedAt())
                .build();
    }

    public ClientChargeResponseDto toClientChargeResponse(ClientChargeView charge) {
        return ClientChargeResponseDto.builder()
                .hashId(charge.getHashId())
                .year(charge.getYear())
                .month(charge.getMonth())
                .periodLabel(charge.getPeriodLabel())
                .expectedAmount(charge.getExpectedAmount())
                .paidAmount(charge.getPaidAmount())
                .balance(charge.getBalance())
                .status(charge.getStatus() != null ? charge.getStatus().name() : null)
                .overdue(charge.isOverdue())
                .build();
    }

    public ClientAccountResponseDto toClientAccountResponse(ClientAccountView account) {
        List<ClientChargeResponseDto> charges = account.getCharges() != null
                ? account.getCharges().stream().map(this::toClientChargeResponse).toList()
                : List.of();
        return ClientAccountResponseDto.builder()
                .clientHashId(account.getClientHashId())
                .totalExpected(account.getTotalExpected())
                .totalPaid(account.getTotalPaid())
                .balance(account.getBalance())
                .credit(account.getCredit())
                .overdueCount(account.getOverdueCount())
                .charges(charges)
                .build();
    }
}
