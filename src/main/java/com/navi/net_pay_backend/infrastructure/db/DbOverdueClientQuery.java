package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.dto.OverdueClientQueryDto;
import com.navi.net_pay_backend.domain.dto.OverdueClientView;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Repository
@RequiredArgsConstructor
public class DbOverdueClientQuery {
    private final NamedParameterJdbcTemplate jdbc;

    private static final String PAID = """
            WITH paid AS (
                SELECT pa.charge_id, SUM(pa.amount) AS amount
                FROM payment_allocation pa JOIN payment p ON p.id = pa.payment_id
                WHERE p.deleted_at IS NULL GROUP BY pa.charge_id
            )
            """;
    private static final String FROM = """
            FROM client c
            JOIN adm_typology ts ON ts.typology_id = c.tp_status
            JOIN app_service s ON s.id = c.service_id
            JOIN client_charge cc ON cc.client_id = c.id AND cc.deleted_at IS NULL
            JOIN billing_period bp ON bp.id = cc.period_id
            LEFT JOIN paid ON paid.charge_id = cc.id
            WHERE c.deleted_at IS NULL AND ts.internal_id <> :deleted
              AND bp.ends_on < CURRENT_DATE AND COALESCE(paid.amount, 0) < cc.expected_amount
            """;
    private static final String SELECT = """
            SELECT c.hash_id, c.business_name, c.contact_name, c.contact_email, c.contact_phone,
                   s.hash_id AS service_hash_id, s.name AS plan_name,
                   ts.internal_id AS status_id, ts.value_1 AS status_code, ts.description AS status_description,
                   COUNT(cc.id) AS overdue_months, SUM(cc.expected_amount - COALESCE(paid.amount, 0)) AS total_debt,
                   STRING_AGG(bp.year || '-' || LPAD(bp.month::text, 2, '0'), ',' ORDER BY bp.year, bp.month) AS periods
            """;
    private static final String GROUP_ORDER = """
            GROUP BY c.id, s.id, ts.typology_id
            ORDER BY total_debt DESC, c.hash_id ASC
            """;

    public List<OverdueClientView> all(Long collectorId, OverdueClientQueryDto query) {
        var params = parameters(collectorId, query);
        return jdbc.query(PAID + SELECT + FROM + filters(collectorId, query) + GROUP_ORDER, params, this::map);
    }

    public long count(Long collectorId, OverdueClientQueryDto query) {
        return jdbc.queryForObject(PAID + "SELECT COUNT(DISTINCT c.id) " + FROM + filters(collectorId, query),
                parameters(collectorId, query), Long.class);
    }

    public PaginatedResult<OverdueClientView> page(OverdueClientQueryDto query) {
        int size = query.getSize() != null && query.getSize() > 0 ? query.getSize() : 10;
        int offset = query.getOffset() != null && query.getOffset() > 0 ? query.getOffset() : 0;
        var params = parameters(null, query).addValue("size", size).addValue("offset", offset);
        long total = count(null, query);
        List<OverdueClientView> rows = total == 0 ? List.of() : jdbc.query(
                PAID + SELECT + FROM + filters(null, query) + GROUP_ORDER + " LIMIT :size OFFSET :offset",
                params, this::map);
        return new PaginatedResult<>(rows, total);
    }

    private String filters(Long collectorId, OverdueClientQueryDto query) {
        String sql = collectorId == null ? "" : " AND c.collector_id = :collector ";
        if (query == null) return sql;
        if (query.getSearch() != null && !query.getSearch().isBlank()) {
            // strpos preserves the previous literal substring semantics (%, _ and backslashes are not wildcards).
            sql += " AND (strpos(lower(c.business_name), :search) > 0 OR strpos(lower(c.contact_name), :search) > 0"
                    + " OR strpos(lower(c.contact_email), :search) > 0) ";
        }
        if (query.getServiceHashId() != null) sql += " AND s.hash_id = :service ";
        if (query.getStatus() != null) sql += " AND ts.internal_id = :status ";
        return sql;
    }

    private MapSqlParameterSource parameters(Long collectorId, OverdueClientQueryDto query) {
        var params = new MapSqlParameterSource("deleted", TypologyEnum.DELETED.getId()).addValue("collector", collectorId);
        if (query != null) {
            params.addValue("search", query.getSearch() == null ? null : query.getSearch().toLowerCase(Locale.ROOT))
                    .addValue("service", query.getServiceHashId()).addValue("status", query.getStatus());
        }
        return params;
    }

    private OverdueClientView map(ResultSet rs, int row) throws SQLException {
        String periods = rs.getString("periods");
        return OverdueClientView.builder()
                .clientHashId(rs.getString("hash_id")).businessName(rs.getString("business_name"))
                .contactName(rs.getString("contact_name")).contactEmail(rs.getString("contact_email"))
                .contactPhone(rs.getString("contact_phone")).serviceHashId(rs.getString("service_hash_id"))
                .planName(rs.getString("plan_name")).statusId(rs.getLong("status_id"))
                .statusCode(rs.getString("status_code")).statusDescription(rs.getString("status_description"))
                .overdueMonths(rs.getLong("overdue_months")).totalDebt(rs.getBigDecimal("total_debt"))
                .overduePeriods(periods == null ? List.of() : Arrays.asList(periods.split(","))).build();
    }
}
