package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.AppPaymentQueryDto;
import com.navi.net_pay_backend.domain.dto.AuditLogQueryDto;
import com.navi.net_pay_backend.domain.dto.CollectorDashboardSummary;
import com.navi.net_pay_backend.domain.dto.CollectorDashboardView;
import com.navi.net_pay_backend.domain.dto.DashboardSummary;
import com.navi.net_pay_backend.domain.dto.DashboardView;
import com.navi.net_pay_backend.domain.dto.MethodCollection;
import com.navi.net_pay_backend.domain.dto.MonthlyMethodCollection;
import com.navi.net_pay_backend.domain.entity.BillingPeriod;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.repository.AppPaymentRepository;
import com.navi.net_pay_backend.domain.repository.AuditLogRepository;
import com.navi.net_pay_backend.domain.repository.ReportRepository;
import com.navi.net_pay_backend.domain.repository.ClientChargeRepository;
import com.navi.net_pay_backend.domain.service.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardUseCase {

    private static final List<TypologyEnum> PAYMENT_METHODS = List.of(
            TypologyEnum.CASH, TypologyEnum.TRANSFER, TypologyEnum.CARD);

    private final ReportRepository reportRepository;
    private final AppPaymentRepository paymentRepository;
    private final AuditLogRepository auditLogRepository;
    private final ClientChargeRepository chargeRepository;
    private final SecurityContext securityContext;

    public DashboardView dashboard(Integer year, Integer month, int recentLimit, int months) {
        YearMonth target = resolveTarget(year, month);
        LocalDate monthStart = target.atDay(1);
        LocalDate monthEnd = target.atEndOfMonth();
        LocalDate nextMonthStart = target.plusMonths(1).atDay(1);

        int safeRecent = recentLimit > 0 ? Math.min(recentLimit, 100) : 5;
        int safeMonths = months > 0 ? Math.min(months, 120) : 6;

        DashboardSummary summary = DashboardSummary.builder()
                .activeClients(reportRepository.countActiveClients())
                .monthCollection(reportRepository.collectionSummary(monthStart, monthEnd).getTotalCollected())
                .overdueClients(countOverdueClients())
                .receiptsIssued(reportRepository.countReceiptsIssuedBetween(
                        monthStart.atStartOfDay(), nextMonthStart.atStartOfDay()))
                .year(target.getYear())
                .month(target.getMonthValue())
                .monthLabel(BillingPeriod.of(target.getYear(), target.getMonthValue()).getLabel())
                .build();

        return DashboardView.builder()
                .summary(summary)
                .recentPayments(recentPayments(safeRecent))
                .recentActivities(recentActivities(safeRecent))
                .clientsByPlan(reportRepository.activeClientsByPlan())
                .collectionsByMethod(normalizeMethods(
                        reportRepository.collectionsByMethodBetween(monthStart, monthEnd)))
                .collectionsByMethodByMonth(methodSeries(target, safeMonths))
                .build();
    }

    public CollectorDashboardView collectorDashboard(Integer year, Integer month, int recentLimit, int months) {
        Long collectorId = securityContext.getUserId()
                .orElseThrow(() -> new DomainException("unauthorized"));

        YearMonth target = resolveTarget(year, month);
        LocalDate monthStart = target.atDay(1);
        LocalDate monthEnd = target.atEndOfMonth();
        LocalDate nextMonthStart = target.plusMonths(1).atDay(1);

        int safeRecent = recentLimit > 0 ? Math.min(recentLimit, 100) : 5;
        int safeMonths = months > 0 ? Math.min(months, 120) : 6;
        chargeRepository.ensureForClients(LocalDate.now(), collectorId);
        var overdue = reportRepository.overdueClients(collectorId);

        CollectorDashboardSummary summary = CollectorDashboardSummary.builder()
                .assignedClients(reportRepository.countActiveClients(collectorId))
                .monthCollection(reportRepository.collectionSummary(monthStart, monthEnd, collectorId).getTotalCollected())
                .overdueClients(overdue.size())
                .receiptsIssued(reportRepository.countReceiptsIssuedBetween(
                        monthStart.atStartOfDay(), nextMonthStart.atStartOfDay(), collectorId))
                .year(target.getYear())
                .month(target.getMonthValue())
                .monthLabel(BillingPeriod.of(target.getYear(), target.getMonthValue()).getLabel())
                .build();

        return CollectorDashboardView.builder()
                .summary(summary)
                .recentPayments(recentPayments(collectorId, safeRecent))
                .overdueClients(overdue)
                .collectionsByMethod(normalizeMethods(
                        reportRepository.collectionsByMethodBetween(monthStart, monthEnd, collectorId)))
                .collectionsByMethodByMonth(collectorMethodSeries(collectorId, target, safeMonths))
                .build();
    }

    private long countOverdueClients() {
        chargeRepository.ensureForClients(LocalDate.now(), null);
        return reportRepository.countOverdueClients(null);
    }

    private List<com.navi.net_pay_backend.domain.entity.AppPayment> recentPayments(int limit) {
        return recentPayments(null, limit);
    }

    private List<com.navi.net_pay_backend.domain.entity.AppPayment> recentPayments(Long collectorId, int limit) {
        AppPaymentQueryDto queryDto = new AppPaymentQueryDto();
        queryDto.setOffset(0);
        queryDto.setSize(limit);
        queryDto.setColumnOrder("createdAt");
        queryDto.setAsc(false);
        queryDto.setCollectorId(collectorId);
        queryDto.setExcludeStatus(TypologyEnum.DELETED.getId());
        return paymentRepository.findAll(queryDto).getItems();
    }

    private List<com.navi.net_pay_backend.domain.entity.AuditLog> recentActivities(int limit) {
        AuditLogQueryDto queryDto = new AuditLogQueryDto();
        queryDto.setOffset(0);
        queryDto.setSize(limit);
        return auditLogRepository.findAll(queryDto).getItems();
    }

    private List<MonthlyMethodCollection> methodSeries(YearMonth target, int months) {
        YearMonth first = target.minusMonths(months - 1L);
        return buildMethodSeries(target, months,
                reportRepository.collectionsByMethodByMonth(first.atDay(1), target.atEndOfMonth()));
    }

    private List<MonthlyMethodCollection> collectorMethodSeries(Long collectorId, YearMonth target, int months) {
        YearMonth first = target.minusMonths(months - 1L);
        return buildMethodSeries(target, months,
                reportRepository.collectionsByMethodByMonth(first.atDay(1), target.atEndOfMonth(), collectorId));
    }

    private List<MonthlyMethodCollection> buildMethodSeries(YearMonth target, int months,
                                                            List<MonthlyMethodCollection> rawSeries) {
        YearMonth first = target.minusMonths(months - 1L);

        Map<Integer, MonthlyMethodCollection> rawByKey = new HashMap<>();
        for (MonthlyMethodCollection monthly : rawSeries) {
            rawByKey.put(monthly.getYear() * 100 + monthly.getMonth(), monthly);
        }

        List<MonthlyMethodCollection> series = new ArrayList<>();
        for (int i = 0; i < months; i++) {
            YearMonth cursor = first.plusMonths(i);
            MonthlyMethodCollection raw = rawByKey.get(cursor.getYear() * 100 + cursor.getMonthValue());
            series.add(MonthlyMethodCollection.builder()
                    .year(cursor.getYear())
                    .month(cursor.getMonthValue())
                    .label(BillingPeriod.of(cursor.getYear(), cursor.getMonthValue()).getLabel())
                    .methods(normalizeMethods(raw != null ? raw.getMethods() : List.of()))
                    .build());
        }
        return series;
    }

    private List<MethodCollection> normalizeMethods(List<MethodCollection> present) {
        Map<Long, MethodCollection> byId = new HashMap<>();
        for (MethodCollection item : present) {
            byId.put(item.getMethodId(), item);
        }

        List<MethodCollection> result = new ArrayList<>();
        for (TypologyEnum method : PAYMENT_METHODS) {
            MethodCollection existing = byId.get(method.getId());
            result.add(existing != null ? existing : MethodCollection.builder()
                    .methodId(method.getId())
                    .methodName(method.name())
                    .methodDescription(description(method))
                    .total(BigDecimal.ZERO)
                    .count(0)
                    .build());
        }
        return result;
    }

    private String description(TypologyEnum method) {
        return switch (method) {
            case CASH -> "Cash";
            case TRANSFER -> "Transfer";
            case CARD -> "Card";
            default -> method.name();
        };
    }

    private YearMonth resolveTarget(Integer year, Integer month) {
        if (year != null && month != null && month >= 1 && month <= 12) {
            return YearMonth.of(year, month);
        }
        return YearMonth.now();
    }
}
