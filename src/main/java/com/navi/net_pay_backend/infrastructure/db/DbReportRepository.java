package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.dto.CollectionSummary;
import com.navi.net_pay_backend.domain.dto.CollectorCollection;
import com.navi.net_pay_backend.domain.dto.MethodCollection;
import com.navi.net_pay_backend.domain.dto.MonthlyMethodCollection;
import com.navi.net_pay_backend.domain.dto.OverdueClientView;
import com.navi.net_pay_backend.domain.dto.PlanClientCount;
import com.navi.net_pay_backend.domain.dto.OverdueClientQueryDto;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class DbReportRepository implements ReportRepository {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final ReportJpaRepository repository;
    private final DbOverdueClientQuery overdueQuery;

    @Override
    public CollectionSummary collectionSummary(LocalDate from, LocalDate to) {
        return collectionSummary(from, to, null);
    }

    @Override
    public CollectionSummary collectionSummary(LocalDate from, LocalDate to, Long collectorId) {
        List<Object[]> rows = collectorId == null
                ? repository.collectionSummary(
                        from, to,
                        TypologyEnum.COMPLETED.getId(),
                        TypologyEnum.FAILED.getId(),
                        TypologyEnum.PENDING.getId(),
                        TypologyEnum.DELETED.getId())
                : repository.collectionSummaryByCollector(
                        from, to, collectorId,
                        TypologyEnum.COMPLETED.getId(),
                        TypologyEnum.FAILED.getId(),
                        TypologyEnum.PENDING.getId(),
                        TypologyEnum.DELETED.getId());

        if (rows.isEmpty()) {
            return CollectionSummary.builder()
                    .totalCollected(ZERO)
                    .completedCount(0)
                    .failedCount(0)
                    .pendingCount(0)
                    .successRate(ZERO)
                    .build();
        }

        Object[] row = rows.get(0);
        long completed = toLong(row[1]);
        long failed = toLong(row[2]);
        long pending = toLong(row[3]);

        return CollectionSummary.builder()
                .totalCollected(toDecimal(row[0]))
                .completedCount(completed)
                .failedCount(failed)
                .pendingCount(pending)
                .successRate(successRate(completed, failed))
                .build();
    }

    @Override
    public List<CollectorCollection> collectionsByCollector(LocalDate from, LocalDate to, int limit) {
        return repository.collectionsByCollector(from, to, TypologyEnum.COMPLETED.getId(), limit).stream()
                .map(row -> CollectorCollection.builder()
                        .collectorHashId((String) row[0])
                        .collectorName((String) row[1])
                        .totalCollected(toDecimal(row[2]))
                        .paymentCount(toLong(row[3]))
                        .build())
                .toList();
    }

    @Override
    public List<OverdueClientView> overdueClients() {
        return overdueClients(null);
    }

    @Override
    public List<OverdueClientView> overdueClients(Long collectorId) {
        return overdueQuery.all(collectorId, null);
    }

    @Override
    public PaginatedResult<OverdueClientView> overdueClientsPage(OverdueClientQueryDto query) {
        return overdueQuery.page(query);
    }

    @Override
    public List<OverdueClientView> filteredOverdueClients(OverdueClientQueryDto query) {
        return overdueQuery.all(null, query);
    }

    @Override
    public long countOverdueClients(Long collectorId) {
        return overdueQuery.count(collectorId, null);
    }

    @Override
    public long countActiveClients() {
        return countActiveClients(null);
    }

    @Override
    public long countActiveClients(Long collectorId) {
        return collectorId == null
                ? repository.countActiveClients(TypologyEnum.ACTIVE.getId())
                : repository.countActiveClientsByCollector(TypologyEnum.ACTIVE.getId(), collectorId);
    }

    @Override
    public long countReceiptsIssuedBetween(LocalDateTime from, LocalDateTime to) {
        return countReceiptsIssuedBetween(from, to, null);
    }

    @Override
    public long countReceiptsIssuedBetween(LocalDateTime from, LocalDateTime to, Long collectorId) {
        return collectorId == null
                ? repository.countReceiptsIssuedBetween(from, to)
                : repository.countReceiptsIssuedBetweenByCollector(from, to, collectorId);
    }

    @Override
    public List<PlanClientCount> activeClientsByPlan() {
        return repository.activeClientsByPlan(TypologyEnum.ACTIVE.getId()).stream()
                .map(row -> PlanClientCount.builder()
                        .planHashId((String) row[0])
                        .planName((String) row[1])
                        .activeClients(toLong(row[2]))
                        .build())
                .toList();
    }

    @Override
    public List<MethodCollection> collectionsByMethodBetween(LocalDate from, LocalDate to) {
        return collectionsByMethodBetween(from, to, null);
    }

    @Override
    public List<MethodCollection> collectionsByMethodBetween(LocalDate from, LocalDate to, Long collectorId) {
        List<Object[]> rows = collectorId == null
                ? repository.collectionsByMethod(from, to, TypologyEnum.COMPLETED.getId())
                : repository.collectionsByMethodByCollector(from, to, TypologyEnum.COMPLETED.getId(), collectorId);
        return rows.stream()
                .map(this::toMethodCollection)
                .toList();
    }

    @Override
    public List<MonthlyMethodCollection> collectionsByMethodByMonth(LocalDate from, LocalDate to) {
        return collectionsByMethodByMonth(from, to, null);
    }

    @Override
    public List<MonthlyMethodCollection> collectionsByMethodByMonth(LocalDate from, LocalDate to, Long collectorId) {
        Map<Integer, MonthlyMethodCollection> byMonth = new LinkedHashMap<>();

        List<Object[]> rows = collectorId == null
                ? repository.collectionsByMethodByMonth(from, to, TypologyEnum.COMPLETED.getId())
                : repository.collectionsByMethodByMonthByCollector(from, to, TypologyEnum.COMPLETED.getId(), collectorId);

        for (Object[] row : rows) {
            int year = (int) toLong(row[0]);
            int month = (int) toLong(row[1]);
            int key = year * 100 + month;

            MonthlyMethodCollection monthly = byMonth.computeIfAbsent(key, k ->
                    MonthlyMethodCollection.builder()
                            .year(year)
                            .month(month)
                            .methods(new ArrayList<>())
                            .build());

            monthly.getMethods().add(toMethodCollection(new Object[]{row[2], row[3], row[4], row[5], row[6]}));
        }

        return new ArrayList<>(byMonth.values());
    }

    private MethodCollection toMethodCollection(Object[] row) {
        return MethodCollection.builder()
                .methodId(row[0] != null ? toLong(row[0]) : null)
                .methodName((String) row[1])
                .methodDescription((String) row[2])
                .total(toDecimal(row[3]))
                .count(toLong(row[4]))
                .build();
    }

    private BigDecimal successRate(long completed, long failed) {
        long attempts = completed + failed;
        if (attempts == 0) {
            return ZERO;
        }
        return BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(attempts), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal toDecimal(Object value) {
        return value == null ? ZERO : new BigDecimal(value.toString());
    }

    private long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }
}
