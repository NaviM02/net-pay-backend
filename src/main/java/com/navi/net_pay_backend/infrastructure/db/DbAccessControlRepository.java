package com.navi.net_pay_backend.infrastructure.db;

import com.navi.net_pay_backend.domain.entity.*;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.dto.RoleQueryDto;
import com.navi.net_pay_backend.domain.exception.EntityAlreadyExistsException;
import com.navi.net_pay_backend.domain.exception.EntityNotFoundException;
import com.navi.net_pay_backend.domain.repository.AccessControlRepository;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class DbAccessControlRepository implements AccessControlRepository {
    private final JdbcTemplate jdbc;
    private static final String ROLE_SELECT = "SELECT r.*, s.internal_id AS status_internal_id FROM app_role r JOIN adm_typology s ON s.typology_id = r.tp_status";

    @Override
    public List<Permission> permissions() {
        return jdbc.query("SELECT hash_id, resource, action FROM app_permission ORDER BY resource, action",
                (rs, n) -> permission(rs));
    }

    @Override
    public List<Role> roles() {
        return loadRoles(ROLE_SELECT + " WHERE s.internal_id <> 51 ORDER BY r.name");
    }

    @Override
    public PaginatedResult<Role> roles(RoleQueryDto query) {
        String where = " WHERE s.internal_id <> 51 AND (CAST(? AS bigint) IS NULL OR s.internal_id = ?) AND position(lower(?) in lower(r.name)) > 0";
        String search = query.getSearch() == null ? "" : query.getSearch().trim();
        String order = "status".equals(query.getColumnOrder()) ? "s.internal_id" : "r.name";
        Long statusId = query.getStatus() == null ? null : TypologyEnum.valueOf(query.getStatus()).getId();
        Long total = jdbc.queryForObject("SELECT count(*) FROM app_role r JOIN adm_typology s ON s.typology_id = r.tp_status" + where,
                Long.class, statusId, statusId, search);
        List<Role> items = loadRoles(ROLE_SELECT + where + " ORDER BY " + order
                        + (query.isAsc() ? " ASC" : " DESC") + ", r.id ASC LIMIT ? OFFSET ?",
                statusId, statusId, search, query.getSize(), query.getOffset());
        return new PaginatedResult<>(items, total == null ? 0L : total);
    }

    private List<Role> loadRoles(String sql, Object... args) {
        List<Role> roles = jdbc.query(sql, (rs, n) -> new Role(rs.getString("hash_id"), rs.getString("name"),
                rs.getString("description"), TypologyEnum.findById(rs.getLong("status_internal_id")).name(), List.of()), args);
        if (roles.isEmpty()) return roles;
        Map<String, List<Permission>> permissions = new HashMap<>();
        jdbc.query("""
                SELECT r.hash_id AS role_hash_id, p.hash_id, p.resource, p.action
                FROM app_role r JOIN app_role_permission rp ON rp.role_id = r.id
                JOIN app_permission p ON p.id = rp.permission_id WHERE r.hash_id IN (
                """ + String.join(",", Collections.nCopies(roles.size(), "?")) + ") ORDER BY p.resource, p.action",
                (org.springframework.jdbc.core.RowCallbackHandler) rs -> permissions.computeIfAbsent(
                        rs.getString("role_hash_id"), key -> new ArrayList<>()).add(permission(rs)),
                roles.stream().map(Role::hashId).toArray());
        return roles.stream().map(r -> new Role(r.hashId(), r.name(), r.description(), r.status(),
                permissions.getOrDefault(r.hashId(), List.of()))).toList();
    }

    @Override
    public Optional<Role> role(String hashId) {
        return loadRoles(ROLE_SELECT + " WHERE r.hash_id = ? AND s.internal_id <> 51", hashId).stream().findFirst();
    }

    @Override
    public Role saveRole(Role role, boolean create) {
        try {
            if (create) jdbc.update("INSERT INTO app_role(hash_id, name, description, tp_status) SELECT ?, ?, ?, typology_id FROM adm_typology WHERE internal_id = ?",
                    role.hashId(), role.name(), role.description(), role.statusInternalId());
            else {
                int updated = jdbc.update("""
                        UPDATE app_role SET name = ?, description = ?, tp_status = (SELECT typology_id FROM adm_typology WHERE internal_id = ?),
                        updated_at = CURRENT_TIMESTAMP WHERE hash_id = ? AND tp_status IN
                        (SELECT typology_id FROM adm_typology WHERE internal_id IN (49, 50))
                        """, role.name(), role.description(), role.statusInternalId(), role.hashId());
                if (updated == 0) throw new DomainException("role_not_editable");
            }
        } catch (DuplicateKeyException ex) {
            throw new EntityAlreadyExistsException("role_name_already_exists");
        }
        Long roleId = jdbc.queryForObject("SELECT id FROM app_role WHERE hash_id = ?", Long.class, role.hashId());
        // Lock serializes concurrent permission replacements on the same role.
        jdbc.queryForObject("SELECT id FROM app_role WHERE id = ? FOR UPDATE", Long.class, roleId);
        jdbc.update("DELETE FROM app_role_permission WHERE role_id = ?", roleId);
        for (Permission permission : role.permissions()) {
            jdbc.update("INSERT INTO app_role_permission(role_id, permission_id) SELECT ?, id FROM app_permission WHERE hash_id = ?",
                    roleId, permission.hashId());
        }
        return role;
    }

    @Override
    public void deleteRole(String hashId) {
        int updated = jdbc.update("""
                UPDATE app_role SET tp_status = (SELECT typology_id FROM adm_typology WHERE internal_id = 51),
                updated_at = CURRENT_TIMESTAMP WHERE hash_id = ? AND tp_status IN
                (SELECT typology_id FROM adm_typology WHERE internal_id IN (49, 50))
                """, hashId);
        if (updated == 0) throw new DomainException("role_not_editable");
    }

    @Override
    public List<UserRole> assignments(Long userId) {
        Map<String, Role> roles = new HashMap<>();
        loadRoles(ROLE_SELECT + " WHERE s.internal_id <> 51 AND r.id IN (SELECT role_id FROM app_user_role WHERE user_id = ?)",
                userId).forEach(r -> roles.put(r.hashId(), r));
        return jdbc.query("""
                SELECT ur.*, r.hash_id AS role_hash_id FROM app_user_role ur
                JOIN app_role r ON r.id = ur.role_id JOIN adm_typology s ON s.typology_id = r.tp_status
                WHERE ur.user_id = ? AND s.internal_id <> 51
                ORDER BY ur.starts_at DESC, ur.id DESC
                """, (rs, n) -> new UserRole(rs.getString("hash_id"), roles.get(rs.getString("role_hash_id")),
                instant(rs, "starts_at"), instant(rs, "ends_at"), instant(rs, "cancelled_at")), userId);
    }

    @Override
    public UserRole assign(Long userId, String roleHashId, Instant startsAt, Instant endsAt) {
        String hashId = UUID.randomUUID().toString();
        int inserted = jdbc.update("""
                INSERT INTO app_user_role(hash_id, user_id, role_id, starts_at, ends_at)
                SELECT ?, ?, r.id, ?, ? FROM app_role r JOIN adm_typology s ON s.typology_id = r.tp_status
                WHERE r.hash_id = ? AND s.internal_id IN (49, 52)
                """, hashId, userId, Timestamp.from(startsAt), endsAt == null ? null : Timestamp.from(endsAt), roleHashId);
        if (inserted == 0) throw new EntityNotFoundException("active_role_not_found");
        return new UserRole(hashId, role(roleHashId).orElseThrow(() -> new EntityNotFoundException("role_not_found")), startsAt, endsAt, null);
    }

    @Override
    public void cancel(Long userId, String hashId, Instant now) {
        int updated = jdbc.update("UPDATE app_user_role SET cancelled_at = COALESCE(cancelled_at, ?) WHERE user_id = ? AND hash_id = ?",
                Timestamp.from(now), userId, hashId);
        if (updated == 0) throw new EntityNotFoundException("user_role_not_found");
    }

    @Override
    public Set<String> effectivePermissions(Long userId, Instant now) {
        return new TreeSet<>(jdbc.queryForList("""
                SELECT DISTINCT p.resource || ':' || p.action FROM app_user_role ur
                JOIN app_role r ON r.id = ur.role_id
                JOIN adm_typology rs ON rs.typology_id = r.tp_status AND rs.internal_id IN (49, 52)
                JOIN app_role_permission rp ON rp.role_id = r.id
                JOIN app_permission p ON p.id = rp.permission_id
                JOIN app_user u ON u.id = ur.user_id
                JOIN adm_typology s ON s.typology_id = u.tp_status AND s.internal_id = 49
                WHERE ur.user_id = ? AND ur.cancelled_at IS NULL
                AND ur.starts_at <= ? AND (ur.ends_at IS NULL OR ur.ends_at > ?)
                """, String.class, userId, Timestamp.from(now), Timestamp.from(now)));
    }

    private Permission permission(ResultSet rs) throws SQLException {
        return new Permission(rs.getString("hash_id"), rs.getString("resource"), rs.getString("action"));
    }
    private Instant instant(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }
}
