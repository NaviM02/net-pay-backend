package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.CollectionReportView;
import com.navi.net_pay_backend.domain.dto.OverdueClientQueryDto;
import com.navi.net_pay_backend.domain.dto.OverdueClientView;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.repository.AppClientRepository;
import com.navi.net_pay_backend.domain.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportUseCase {

    private final ReportRepository reportRepository;
    private final AppClientRepository clientRepository;
    private final ClientChargeGenerator chargeGenerator;

    public CollectionReportView collections(LocalDate from, LocalDate to, int collectorLimit) {
        LocalDate fromDate = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate toDate = to != null ? to : LocalDate.now();
        if (fromDate.isAfter(toDate)) {
            LocalDate swap = fromDate;
            fromDate = toDate;
            toDate = swap;
        }

        return CollectionReportView.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .summary(reportRepository.collectionSummary(fromDate, toDate))
                .byCollector(reportRepository.collectionsByCollector(fromDate, toDate, collectorLimit))
                .build();
    }

    public PaginatedResult<OverdueClientView> overdueClients(OverdueClientQueryDto queryDto) {
        ensureCharges();

        List<OverdueClientView> filtered = reportRepository.overdueClients().stream()
                .filter(view -> matchesSearch(view, queryDto.getSearch()))
                .filter(view -> queryDto.getServiceHashId() == null
                        || queryDto.getServiceHashId().equals(view.getServiceHashId()))
                .filter(view -> queryDto.getStatus() == null
                        || queryDto.getStatus().equals(view.getStatusId()))
                .toList();

        int size = queryDto.getSize() != null && queryDto.getSize() > 0 ? queryDto.getSize() : 10;
        int offset = queryDto.getOffset() != null && queryDto.getOffset() > 0 ? queryDto.getOffset() : 0;
        int start = Math.min(offset, filtered.size());
        int end = Math.min(start + size, filtered.size());

        return new PaginatedResult<>(filtered.subList(start, end), filtered.size());
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
}
