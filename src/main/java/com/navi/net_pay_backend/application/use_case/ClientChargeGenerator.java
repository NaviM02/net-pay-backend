package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.BillingPeriod;
import com.navi.net_pay_backend.domain.entity.ClientCharge;
import com.navi.net_pay_backend.domain.repository.BillingPeriodRepository;
import com.navi.net_pay_backend.domain.repository.ClientChargeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClientChargeGenerator {

    private final BillingPeriodRepository billingPeriodRepository;
    private final ClientChargeRepository clientChargeRepository;

    public List<ClientCharge> ensureUpTo(AppClient client, LocalDate upTo) {
        LocalDate start = client.getCustomerSince() != null ? client.getCustomerSince() : LocalDate.now();
        LocalDate end = upTo != null && upTo.isAfter(LocalDate.now()) ? upTo : LocalDate.now();

        YearMonth startMonth = YearMonth.from(start);
        YearMonth endMonth = YearMonth.from(end);

        List<ClientCharge> charges = new ArrayList<>();
        for (YearMonth cursor = startMonth; !cursor.isAfter(endMonth); cursor = cursor.plusMonths(1)) {
            BillingPeriod period = ensurePeriod(cursor.getYear(), cursor.getMonthValue());
            charges.add(ensureCharge(client, period));
        }
        return charges;
    }

    public BillingPeriod ensurePeriod(int year, int month) {
        return billingPeriodRepository.findByYearAndMonth(year, month)
                .orElseGet(() -> billingPeriodRepository.save(BillingPeriod.of(year, month)));
    }

    public ClientCharge ensureCharge(AppClient client, BillingPeriod period) {
        return clientChargeRepository.findByClientIdAndPeriodId(client.getId(), period.getId())
                .orElseGet(() -> clientChargeRepository.save(ClientCharge.builder()
                        .hashId(UUID.randomUUID().toString())
                        .client(client)
                        .period(period)
                        .service(client.getService())
                        .expectedAmount(client.getService() != null && client.getService().getMonthlyPrice() != null
                                ? client.getService().getMonthlyPrice()
                                : BigDecimal.ZERO)
                        .build()));
    }
}
