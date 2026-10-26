package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/** Set-based equivalent of ClientChargeGenerator; never reprices existing charges. */
@Component
@RequiredArgsConstructor
public class DbChargeSynchronization {
    private final NamedParameterJdbcTemplate jdbc;

    @Transactional
    public void ensureForClients(LocalDate through, Long collectorId) {
        var params = new MapSqlParameterSource().addValue("through", through)
                .addValue("deleted", TypologyEnum.DELETED.getId()).addValue("collector", collectorId);
        String scope = collectorId == null ? "" : " AND c.collector_id = :collector";
        jdbc.update("""
                INSERT INTO billing_period (year, month, starts_on, ends_on)
                SELECT EXTRACT(YEAR FROM m)::integer, EXTRACT(MONTH FROM m)::integer,
                       m::date, (m + interval '1 month - 1 day')::date
                FROM generate_series(
                    (SELECT date_trunc('month', MIN(COALESCE(c.customer_since, CAST(:through AS date))))
                     FROM client c JOIN adm_typology ts ON ts.typology_id = c.tp_status
                     WHERE c.deleted_at IS NULL AND ts.internal_id <> :deleted
                """ + scope + """
                    ), date_trunc('month', CAST(:through AS date)), interval '1 month') AS m
                WHERE NOT EXISTS (SELECT 1 FROM billing_period bp
                                  WHERE bp.year = EXTRACT(YEAR FROM m) AND bp.month = EXTRACT(MONTH FROM m))
                ON CONFLICT (year, month) DO NOTHING
                """, params);
        jdbc.update("""
                INSERT INTO client_charge (hash_id, client_id, period_id, service_id, expected_amount)
                SELECT gen_random_uuid()::text, c.id, bp.id, c.service_id, s.monthly_price
                FROM client c
                JOIN adm_typology ts ON ts.typology_id = c.tp_status
                JOIN app_service s ON s.id = c.service_id
                JOIN billing_period bp
                  ON bp.starts_on >= date_trunc('month', COALESCE(c.customer_since, CAST(:through AS date)))::date
                 AND bp.starts_on <= CAST(:through AS date)
                WHERE c.deleted_at IS NULL AND ts.internal_id <> :deleted
                """ + scope + """
                  AND NOT EXISTS (SELECT 1 FROM client_charge cc WHERE cc.client_id = c.id AND cc.period_id = bp.id)
                ON CONFLICT (client_id, period_id) DO NOTHING
                """, params);
    }
}
