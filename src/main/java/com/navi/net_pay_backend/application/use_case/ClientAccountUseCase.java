package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.domain.dto.ClientAccountView;
import com.navi.net_pay_backend.domain.dto.ClientChargeView;
import com.navi.net_pay_backend.domain.entity.AppClient;
import com.navi.net_pay_backend.domain.entity.ClientCharge;
import com.navi.net_pay_backend.domain.enums.ChargeStatusEnum;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AppClientRepository;
import com.navi.net_pay_backend.domain.repository.AppPaymentRepository;
import com.navi.net_pay_backend.domain.repository.PaymentAllocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientAccountUseCase {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final AppClientRepository clientRepository;
    private final AppPaymentRepository paymentRepository;
    private final ClientChargeGenerator chargeGenerator;
    private final PaymentAllocationRepository allocationRepository;

    public List<ClientChargeView> charges(String clientHashId) {
        return buildViews(findClient(clientHashId));
    }

    public ClientAccountView account(String clientHashId) {
        AppClient client = findClient(clientHashId);
        List<ClientChargeView> views = buildViews(client);

        BigDecimal totalExpected = ZERO;
        BigDecimal totalAssigned = ZERO;
        int overdueCount = 0;

        for (ClientChargeView view : views) {
            totalExpected = totalExpected.add(view.getExpectedAmount());
            totalAssigned = totalAssigned.add(view.getPaidAmount());
            if (view.isOverdue()) {
                overdueCount++;
            }
        }

        BigDecimal totalPaid = paymentRepository.sumAmountByClientId(client.getId(), TypologyEnum.DELETED.getId());
        BigDecimal balance = totalExpected.subtract(totalAssigned);
        BigDecimal credit = totalPaid.subtract(totalAssigned);
        if (credit.signum() < 0) {
            credit = ZERO;
        }

        return ClientAccountView.builder()
                .clientHashId(client.getHashId())
                .totalExpected(totalExpected)
                .totalPaid(totalPaid)
                .balance(balance)
                .credit(credit)
                .overdueCount(overdueCount)
                .charges(views)
                .build();
    }

    private AppClient findClient(String clientHashId) {
        return clientRepository.findByHashIdAndStatusNot(clientHashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("client_not_found"));
    }

    private List<ClientChargeView> buildViews(AppClient client) {
        LocalDate today = LocalDate.now();
        List<ClientChargeView> views = new ArrayList<>();

        for (ClientCharge charge : chargeGenerator.ensureUpTo(client, today)) {
            BigDecimal paid = allocationRepository.sumAmountByChargeId(charge.getId());
            BigDecimal expected = charge.getExpectedAmount() != null ? charge.getExpectedAmount() : ZERO;
            BigDecimal balance = expected.subtract(paid);

            boolean overdue = charge.getPeriod() != null
                    && charge.getPeriod().getEndsOn() != null
                    && charge.getPeriod().getEndsOn().isBefore(today)
                    && paid.compareTo(expected) < 0;

            ChargeStatusEnum status;
            if (paid.signum() <= 0) {
                status = overdue ? ChargeStatusEnum.OVERDUE : ChargeStatusEnum.PENDING;
            } else if (paid.compareTo(expected) >= 0) {
                status = ChargeStatusEnum.PAID;
            } else {
                status = overdue ? ChargeStatusEnum.OVERDUE : ChargeStatusEnum.PARTIAL;
            }

            views.add(ClientChargeView.builder()
                    .hashId(charge.getHashId())
                    .year(charge.getPeriod() != null ? charge.getPeriod().getYear() : null)
                    .month(charge.getPeriod() != null ? charge.getPeriod().getMonth() : null)
                    .periodLabel(charge.getPeriod() != null ? charge.getPeriod().getLabel() : null)
                    .expectedAmount(expected)
                    .paidAmount(paid)
                    .balance(balance)
                    .status(status)
                    .overdue(overdue)
                    .build());
        }
        return views;
    }
}
