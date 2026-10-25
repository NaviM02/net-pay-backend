package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.OverdueClientView;
import com.navi.net_pay_backend.domain.exception.DomainException;
import com.navi.net_pay_backend.domain.repository.ClientChargeRepository;
import com.navi.net_pay_backend.domain.repository.ReportRepository;
import com.navi.net_pay_backend.domain.service.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OverdueNotificationUseCase {
    private final ReportRepository reportRepository;
    private final ClientChargeRepository chargeRepository;
    private final SecurityContext securityContext;

    @Transactional
    public List<OverdueClientView> findOverdueClients(boolean includeAllClients) {
        Long collectorId = includeAllClients ? null : securityContext.getUserId()
                .orElseThrow(() -> new DomainException("unauthorized"));

        chargeRepository.ensureForClients(LocalDate.now(), collectorId);
        return reportRepository.overdueClients(collectorId);
    }
}
