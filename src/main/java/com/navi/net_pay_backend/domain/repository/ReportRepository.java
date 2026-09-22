package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.dto.CollectionSummary;
import com.navi.net_pay_backend.domain.dto.CollectorCollection;
import com.navi.net_pay_backend.domain.dto.MethodCollection;
import com.navi.net_pay_backend.domain.dto.MonthlyMethodCollection;
import com.navi.net_pay_backend.domain.dto.OverdueClientView;
import com.navi.net_pay_backend.domain.dto.PlanClientCount;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository {

    CollectionSummary collectionSummary(LocalDate from, LocalDate to);

    CollectionSummary collectionSummary(LocalDate from, LocalDate to, Long collectorId);

    List<CollectorCollection> collectionsByCollector(LocalDate from, LocalDate to, int limit);

    List<OverdueClientView> overdueClients();

    List<OverdueClientView> overdueClients(Long collectorId);

    long countActiveClients();

    long countActiveClients(Long collectorId);

    long countReceiptsIssuedBetween(LocalDateTime from, LocalDateTime to);

    long countReceiptsIssuedBetween(LocalDateTime from, LocalDateTime to, Long collectorId);

    List<PlanClientCount> activeClientsByPlan();

    List<MethodCollection> collectionsByMethodBetween(LocalDate from, LocalDate to);

    List<MethodCollection> collectionsByMethodBetween(LocalDate from, LocalDate to, Long collectorId);

    List<MonthlyMethodCollection> collectionsByMethodByMonth(LocalDate from, LocalDate to);

    List<MonthlyMethodCollection> collectionsByMethodByMonth(LocalDate from, LocalDate to, Long collectorId);
}
