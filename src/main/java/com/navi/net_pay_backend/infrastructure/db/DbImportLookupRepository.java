package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.repository.ImportLookupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class DbImportLookupRepository implements ImportLookupRepository {
    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public List<Long> findClientIds(String businessName, String email, String phone) {
        if (businessName == null && email == null && phone == null) return List.of();
        String sql = """
                SELECT c.id FROM client c JOIN adm_typology ts ON ts.typology_id = c.tp_status
                WHERE c.deleted_at IS NULL AND ts.internal_id <> :deleted
                """;
        if (businessName != null) sql += " AND lower(btrim(c.business_name)) = lower(:name)";
        if (email != null) sql += " AND lower(btrim(c.contact_email)) = lower(:email)";
        if (phone != null) sql += " AND btrim(c.contact_phone) = :phone";
        return jdbc.queryForList(sql + " ORDER BY c.id LIMIT 2", params()
                .addValue("name", businessName).addValue("email", email).addValue("phone", phone), Long.class);
    }

    @Override
    public List<Long> findUserIds(String email, String fullName) {
        if (email == null && fullName == null) return List.of();
        String sql = """
                SELECT u.id FROM app_user u JOIN adm_typology ts ON ts.typology_id = u.tp_status
                WHERE u.deleted_at IS NULL AND ts.internal_id <> :deleted
                """;
        if (email != null) sql += " AND lower(btrim(u.email)) = lower(:email)";
        if (fullName != null) sql += " AND lower(btrim(u.full_name)) = lower(:name)";
        return jdbc.queryForList(sql + " ORDER BY u.id LIMIT 2", params()
                .addValue("email", email).addValue("name", fullName), Long.class);
    }

    @Override
    public void lockUserEmail(String email) {
        // Serialize imports for this email without imposing new uniqueness rules on existing user data.
        jdbc.queryForList("SELECT pg_advisory_xact_lock(hashtextextended(lower(:email), 15001))",
                new MapSqlParameterSource("email", email));
    }

    @Override
    public void lockClient(Long clientId) {
        jdbc.queryForList("SELECT id FROM client WHERE id = :id FOR UPDATE", new MapSqlParameterSource("id", clientId));
    }

    private MapSqlParameterSource params() {
        return new MapSqlParameterSource("deleted", TypologyEnum.DELETED.getId());
    }
}
