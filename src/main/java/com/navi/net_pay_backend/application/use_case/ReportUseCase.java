package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.CollectionReportView;
import com.navi.net_pay_backend.domain.dto.CollectorCollection;
import com.navi.net_pay_backend.domain.dto.OverdueClientQueryDto;
import com.navi.net_pay_backend.domain.dto.OverdueClientView;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.dto.ReportTable;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.repository.AppClientRepository;
import com.navi.net_pay_backend.domain.repository.ReportRepository;
import com.navi.net_pay_backend.domain.service.ReportTableCsvExporter;
import com.navi.net_pay_backend.domain.service.ReportTablePdfExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportUseCase {

    private final ReportRepository reportRepository;
    private final AppClientRepository clientRepository;
    private final ClientChargeGenerator chargeGenerator;
    private final ReportTablePdfExporter pdfExporter;
    private final ReportTableCsvExporter csvExporter;

    public CollectionReportView collections(LocalDate from, LocalDate to, int collectorLimit) {
        LocalDate[] range = normalizeRange(from, to);
        return CollectionReportView.builder()
                .fromDate(range[0])
                .toDate(range[1])
                .summary(reportRepository.collectionSummary(range[0], range[1]))
                .byCollector(reportRepository.collectionsByCollector(range[0], range[1], collectorLimit))
                .build();
    }

    public PaginatedResult<OverdueClientView> overdueClients(OverdueClientQueryDto queryDto) {
        List<OverdueClientView> filtered = filteredOverdueClients(queryDto);

        int size = queryDto.getSize() != null && queryDto.getSize() > 0 ? queryDto.getSize() : 10;
        int offset = queryDto.getOffset() != null && queryDto.getOffset() > 0 ? queryDto.getOffset() : 0;
        int start = Math.min(offset, filtered.size());
        int end = Math.min(start + size, filtered.size());

        return new PaginatedResult<>(filtered.subList(start, end), filtered.size());
    }

    // ── Export ──────────────────────────────────────────────────────────────

    public byte[] exportCollectionsPdf(LocalDate from, LocalDate to, int collectorLimit) {
        return pdfExporter.export(collectionsTable(collections(from, to, collectorLimit)));
    }

    public byte[] exportCollectionsCsv(LocalDate from, LocalDate to, int collectorLimit) {
        return csvExporter.export(collectionsTable(collections(from, to, collectorLimit)));
    }

    public byte[] exportOverdueClientsPdf(OverdueClientQueryDto queryDto) {
        return pdfExporter.export(overdueClientsTable(filteredOverdueClients(queryDto)));
    }

    public byte[] exportOverdueClientsCsv(OverdueClientQueryDto queryDto) {
        return csvExporter.export(overdueClientsTable(filteredOverdueClients(queryDto)));
    }

    private ReportTable collectionsTable(CollectionReportView view) {
        List<List<String>> rows = view.getByCollector().stream()
                .map(this::collectorRow)
                .toList();

        return ReportTable.builder()
                .title("Reporte · Cobros por período")
                .subtitle("Del " + view.getFromDate() + " al " + view.getToDate())
                .headers(List.of("Cobrador", "Total cobrado", "N.º de pagos"))
                .rightAlignedColumns(List.of(1, 2))
                .rows(rows)
                .build();
    }

    private List<String> collectorRow(CollectorCollection item) {
        return List.of(
                orDash(item.getCollectorName(), "Sin asignar"),
                money(item.getTotalCollected()),
                String.valueOf(item.getPaymentCount()));
    }

    private ReportTable overdueClientsTable(List<OverdueClientView> clients) {
        List<List<String>> rows = clients.stream()
                .map(this::overdueClientRow)
                .toList();

        return ReportTable.builder()
                .title("Reporte · Clientes en mora")
                .subtitle(clients.size() + " cliente(s)")
                .headers(List.of("Cliente", "Contacto", "Plan", "Estado",
                        "Meses sin pagar", "Períodos", "Deuda total"))
                .rightAlignedColumns(List.of(4, 6))
                .rows(rows)
                .build();
    }

    private List<String> overdueClientRow(OverdueClientView view) {
        String periods = view.getOverduePeriods() != null && !view.getOverduePeriods().isEmpty()
                ? String.join(", ", view.getOverduePeriods())
                : "—";
        return List.of(
                orDash(view.getBusinessName(), "—"),
                orDash(view.getContactName(), "—"),
                orDash(view.getPlanName(), "—"),
                orDash(view.getStatusDescription(), "—"),
                String.valueOf(view.getOverdueMonths()),
                periods,
                money(view.getTotalDebt()));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private List<OverdueClientView> filteredOverdueClients(OverdueClientQueryDto queryDto) {
        ensureCharges();

        return reportRepository.overdueClients().stream()
                .filter(view -> matchesSearch(view, queryDto.getSearch()))
                .filter(view -> queryDto.getServiceHashId() == null
                        || queryDto.getServiceHashId().equals(view.getServiceHashId()))
                .filter(view -> queryDto.getStatus() == null
                        || queryDto.getStatus().equals(view.getStatusId()))
                .toList();
    }

    private LocalDate[] normalizeRange(LocalDate from, LocalDate to) {
        LocalDate fromDate = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate toDate = to != null ? to : LocalDate.now();
        if (fromDate.isAfter(toDate)) {
            return new LocalDate[]{toDate, fromDate};
        }
        return new LocalDate[]{fromDate, toDate};
    }

    private void ensureCharges() {
        LocalDate today = LocalDate.now();
        for (AppClient client : clientRepository.findAllNotDeleted()) {
            chargeGenerator.ensureUpTo(client, today);
        }
    }

    private boolean matchesSearch(OverdueClientView view, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String term = search.toLowerCase();
        return contains(view.getBusinessName(), term)
                || contains(view.getContactName(), term)
                || contains(view.getContactEmail(), term);
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }

    private String money(BigDecimal value) {
        BigDecimal amount = value != null ? value : BigDecimal.ZERO;
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String orDash(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
