package com.navi.net_pay_backend.infrastructure.http.mapper;

import com.navi.net_pay_backend.domain.dto.ClientAccountView;
import com.navi.net_pay_backend.domain.dto.ClientChargeView;
import com.navi.net_pay_backend.domain.dto.CollectionReportView;
import com.navi.net_pay_backend.domain.dto.CollectionSummary;
import com.navi.net_pay_backend.domain.dto.CollectorCollection;
import com.navi.net_pay_backend.domain.dto.CollectorDashboardSummary;
import com.navi.net_pay_backend.domain.dto.CollectorDashboardView;
import com.navi.net_pay_backend.domain.dto.DashboardSummary;
import com.navi.net_pay_backend.domain.dto.DashboardView;
import com.navi.net_pay_backend.domain.dto.MethodCollection;
import com.navi.net_pay_backend.domain.dto.MonthlyMethodCollection;
import com.navi.net_pay_backend.domain.dto.OverdueClientView;
import com.navi.net_pay_backend.domain.dto.PlanClientCount;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.AuditLog;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
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
import com.navi.net_pay_backend.infrastructure.http.dto.AuditLogResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.ClientAccountResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.ClientChargeResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.CollectionReportResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.CollectionSummaryDto;
import com.navi.net_pay_backend.infrastructure.http.dto.CollectorCollectionDto;
import com.navi.net_pay_backend.infrastructure.http.dto.CollectorDashboardResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.CollectorDashboardSummaryDto;
import com.navi.net_pay_backend.infrastructure.http.dto.DashboardResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.DashboardSummaryDto;
import com.navi.net_pay_backend.infrastructure.http.dto.MethodCollectionDto;
import com.navi.net_pay_backend.infrastructure.http.dto.MonthlyMethodCollectionDto;
import com.navi.net_pay_backend.infrastructure.http.dto.OverdueClientResponseDto;
import com.navi.net_pay_backend.infrastructure.http.dto.PlanClientCountDto;
import com.navi.net_pay_backend.infrastructure.http.dto.ReceiptResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HttpMapper {

    public AppUserResponseDto toAppUserResponse(AppUser user) {
        return AppUserResponseDto.builder()
                .hashId(user.getHashId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .twoFactorEnabled(Boolean.TRUE.equals(user.getTwoFactorEnabled()))
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
                .attachmentUrl(payment.getAttachmentUrl())
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

    public AuditLogResponseDto toAuditLogResponse(AuditLog log) {
        TypologyEnum level = log.getLevelId() != null ? TypologyEnum.findById(log.getLevelId()) : null;
        return AuditLogResponseDto.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .userEmail(log.getUserEmail())
                .action(log.getAction())
                .detail(log.getDetail())
                .entityType(log.getEntityType())
                .entityHashId(log.getEntityHashId())
                .changes(log.getChanges())
                .ipAddress(log.getIpAddress())
                .level(log.getLevelId())
                .levelName(level != null ? level.name() : null)
                .createdAt(log.getCreatedAt())
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
                .totalCharges(account.getTotalCharges())
                .charges(charges)
                .build();
    }

    public CollectionSummaryDto toCollectionSummaryDto(CollectionSummary summary) {
        return CollectionSummaryDto.builder()
                .totalCollected(summary.getTotalCollected())
                .completedCount(summary.getCompletedCount())
                .failedCount(summary.getFailedCount())
                .pendingCount(summary.getPendingCount())
                .successRate(summary.getSuccessRate())
                .build();
    }

    public CollectorCollectionDto toCollectorCollectionDto(CollectorCollection item) {
        return CollectorCollectionDto.builder()
                .collectorHashId(item.getCollectorHashId())
                .collectorName(item.getCollectorName())
                .totalCollected(item.getTotalCollected())
                .paymentCount(item.getPaymentCount())
                .build();
    }

    public CollectionReportResponseDto toCollectionReportResponse(CollectionReportView view) {
        return CollectionReportResponseDto.builder()
                .fromDate(view.getFromDate())
                .toDate(view.getToDate())
                .summary(toCollectionSummaryDto(view.getSummary()))
                .byCollector(view.getByCollector() != null
                        ? view.getByCollector().stream().map(this::toCollectorCollectionDto).toList()
                        : List.of())
                .build();
    }

    public OverdueClientResponseDto toOverdueClientResponse(OverdueClientView view) {
        return OverdueClientResponseDto.builder()
                .clientHashId(view.getClientHashId())
                .businessName(view.getBusinessName())
                .contactName(view.getContactName())
                .contactEmail(view.getContactEmail())
                .contactPhone(view.getContactPhone())
                .serviceHashId(view.getServiceHashId())
                .planName(view.getPlanName())
                .statusId(view.getStatusId())
                .statusCode(view.getStatusCode())
                .statusDescription(view.getStatusDescription())
                .overdueMonths(view.getOverdueMonths())
                .totalDebt(view.getTotalDebt())
                .overduePeriods(view.getOverduePeriods())
                .build();
    }

    public DashboardSummaryDto toDashboardSummaryDto(DashboardSummary summary) {
        return DashboardSummaryDto.builder()
                .activeClients(summary.getActiveClients())
                .monthCollection(summary.getMonthCollection())
                .overdueClients(summary.getOverdueClients())
                .receiptsIssued(summary.getReceiptsIssued())
                .year(summary.getYear())
                .month(summary.getMonth())
                .monthLabel(summary.getMonthLabel())
                .build();
    }

    public PlanClientCountDto toPlanClientCountDto(PlanClientCount item) {
        return PlanClientCountDto.builder()
                .planHashId(item.getPlanHashId())
                .planName(item.getPlanName())
                .activeClients(item.getActiveClients())
                .build();
    }

    public MethodCollectionDto toMethodCollectionDto(MethodCollection item) {
        return MethodCollectionDto.builder()
                .methodId(item.getMethodId())
                .methodName(item.getMethodName())
                .methodDescription(item.getMethodDescription())
                .total(item.getTotal())
                .count(item.getCount())
                .build();
    }

    public MonthlyMethodCollectionDto toMonthlyMethodCollectionDto(MonthlyMethodCollection item) {
        return MonthlyMethodCollectionDto.builder()
                .year(item.getYear())
                .month(item.getMonth())
                .label(item.getLabel())
                .methods(item.getMethods() != null
                        ? item.getMethods().stream().map(this::toMethodCollectionDto).toList()
                        : List.of())
                .build();
    }

    public DashboardResponseDto toDashboardResponse(DashboardView view) {
        return DashboardResponseDto.builder()
                .summary(toDashboardSummaryDto(view.getSummary()))
                .recentPayments(view.getRecentPayments() != null
                        ? view.getRecentPayments().stream().map(this::toAppPaymentResponse).toList()
                        : List.of())
                .recentActivities(view.getRecentActivities() != null
                        ? view.getRecentActivities().stream().map(this::toAuditLogResponse).toList()
                        : List.of())
                .clientsByPlan(view.getClientsByPlan() != null
                        ? view.getClientsByPlan().stream().map(this::toPlanClientCountDto).toList()
                        : List.of())
                .collectionsByMethod(view.getCollectionsByMethod() != null
                        ? view.getCollectionsByMethod().stream().map(this::toMethodCollectionDto).toList()
                        : List.of())
                .collectionsByMethodByMonth(view.getCollectionsByMethodByMonth() != null
                        ? view.getCollectionsByMethodByMonth().stream().map(this::toMonthlyMethodCollectionDto).toList()
                        : List.of())
                .build();
    }

    public CollectorDashboardSummaryDto toCollectorDashboardSummaryDto(CollectorDashboardSummary summary) {
        return CollectorDashboardSummaryDto.builder()
                .assignedClients(summary.getAssignedClients())
                .monthCollection(summary.getMonthCollection())
                .overdueClients(summary.getOverdueClients())
                .receiptsIssued(summary.getReceiptsIssued())
                .year(summary.getYear())
                .month(summary.getMonth())
                .monthLabel(summary.getMonthLabel())
                .build();
    }

    public CollectorDashboardResponseDto toCollectorDashboardResponse(CollectorDashboardView view) {
        return CollectorDashboardResponseDto.builder()
                .summary(toCollectorDashboardSummaryDto(view.getSummary()))
                .recentPayments(view.getRecentPayments() != null
                        ? view.getRecentPayments().stream().map(this::toAppPaymentResponse).toList()
                        : List.of())
                .overdueClients(view.getOverdueClients() != null
                        ? view.getOverdueClients().stream().map(this::toOverdueClientResponse).toList()
                        : List.of())
                .collectionsByMethod(view.getCollectionsByMethod() != null
                        ? view.getCollectionsByMethod().stream().map(this::toMethodCollectionDto).toList()
                        : List.of())
                .collectionsByMethodByMonth(view.getCollectionsByMethodByMonth() != null
                        ? view.getCollectionsByMethodByMonth().stream().map(this::toMonthlyMethodCollectionDto).toList()
                        : List.of())
                .build();
    }
}
