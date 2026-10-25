package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.entity.ClientCharge;

import java.util.List;
import java.util.Optional;

public interface ClientChargeRepository {
    /** Creates only missing obligations through the given month, optionally scoped to a collector. */
    void ensureForClients(java.time.LocalDate through, Long collectorId);

    Optional<ClientCharge> findByClientIdAndPeriodId(Long clientId, Long periodId);

    Optional<ClientCharge> findByHashId(String hashId);

    List<ClientCharge> findByClientIdOrderByPeriod(Long clientId);

    ClientCharge save(ClientCharge charge);
}
