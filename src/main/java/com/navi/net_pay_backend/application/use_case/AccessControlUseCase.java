package com.navi.net_pay_backend.application.use_case;

import com.navi.net_pay_backend.application.model.RoleRequest;
import com.navi.net_pay_backend.application.model.UserRoleRequest;
import com.navi.net_pay_backend.domain.entity.*;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.dto.RoleQueryDto;
import com.navi.net_pay_backend.domain.enums.TypologyEnum;
import com.navi.net_pay_backend.domain.exception.*;
import com.navi.net_pay_backend.domain.repository.AccessControlRepository;
import com.navi.net_pay_backend.domain.repository.AppUserRepository;
import com.navi.net_pay_backend.domain.service.AuditLogService;
import com.navi.net_pay_backend.domain.service.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccessControlUseCase {
    private final AccessControlRepository access;
    private final AppUserRepository users;
    private final SecurityContext securityContext;
    private final AuditLogService audit;

    public List<Permission> permissions() { return access.permissions(); }
    public List<Role> roles() { return access.roles(); }
    public PaginatedResult<Role> roles(RoleQueryDto query) {
        if (query.getOffset() == null || query.getOffset() < 0 || query.getSize() == null
                || query.getSize() < 1 || query.getSize() > 100) throw new DomainException("pagination_invalid");
        if (query.getStatus() != null && !Set.of("ACTIVE", "INACTIVE", "LOCKED").contains(query.getStatus()))
            throw new DomainException("role_status_invalid");
        return access.roles(query);
    }
    public Role role(String hashId) {
        return access.role(hashId).orElseThrow(() -> new EntityNotFoundException("role_not_found"));
    }

    @Transactional
    public Role saveRole(String hashId, RoleRequest request) {
        if (hashId != null) requireEditable(role(hashId));
        if (request.name() == null || request.name().isBlank() || request.name().trim().length() > 150)
            throw new DomainException("role_name_invalid");
        if (request.description() != null && request.description().length() > 500)
            throw new DomainException("role_description_invalid");
        String status = request.status() == null ? "ACTIVE" : request.status();
        if (!Set.of("ACTIVE", "INACTIVE").contains(status)) throw new DomainException("role_status_invalid");
        if (request.permissionHashIds() == null) throw new DomainException("role_permissions_required");
        Set<String> ids = new HashSet<>(request.permissionHashIds());
        List<Permission> permissions = access.permissions().stream().filter(p -> ids.contains(p.hashId())).toList();
        if (permissions.size() != ids.size()) throw new DomainException("permission_not_found");
        Role saved = access.saveRole(new Role(hashId == null ? UUID.randomUUID().toString() : hashId,
                request.name().trim(), request.description(), status, permissions), hashId == null);
        audit.record(hashId == null ? "role_created" : "role_updated", "Guardó rol", "ROLE", saved.hashId(),
                Map.of("name", saved.name(), "status", saved.status(), "permissions", permissions.stream().map(Permission::authority).toList()));
        return saved;
    }

    @Transactional
    public void deleteRole(String hashId) {
        requireEditable(role(hashId));
        access.deleteRole(hashId);
        audit.record("role_deleted", "Eliminó rol", "ROLE", hashId, Map.of());
    }

    public List<UserRole> assignments(String userHashId) { return access.assignments(user(userHashId).getId()); }

    @Transactional
    public UserRole assign(String userHashId, UserRoleRequest request) {
        AppUser user = user(userHashId);
        Role role = role(request.roleHashId());
        if (!role.isAssignable()) throw new DomainException("role_inactive");
        Instant startsAt = request.startsAt() == null ? Instant.now() : request.startsAt();
        if (request.endsAt() != null && !request.endsAt().isAfter(startsAt))
            throw new DomainException("role_date_range_invalid");
        UserRole assignment = access.assign(user.getId(), role.hashId(), startsAt, request.endsAt());
        audit.record("user_role_assigned", "Asignó rol", "USER", userHashId,
                Map.of("assignmentHashId", assignment.hashId(), "roleHashId", role.hashId()));
        return assignment;
    }

    @Transactional
    public void cancel(String userHashId, String assignmentHashId) {
        access.cancel(user(userHashId).getId(), assignmentHashId, Instant.now());
        audit.record("user_role_cancelled", "Canceló rol", "USER", userHashId, Map.of("assignmentHashId", assignmentHashId));
    }

    public Set<String> effectivePermissions(Long userId) { return access.effectivePermissions(userId, Instant.now()); }
    public List<UserRole> myAssignments() { return access.assignments(currentUserId()); }
    public Set<String> myPermissions() { return effectivePermissions(currentUserId()); }

    private Long currentUserId() {
        return securityContext.getUserId().orElseThrow(() -> new UnauthorizeException("unauthorized"));
    }
    private void requireEditable(Role role) {
        if ("LOCKED".equals(role.status())) throw new DomainException("role_locked");
    }
    private AppUser user(String hashId) {
        return users.findByPublicIdAndStatusNot(hashId, TypologyEnum.DELETED.getId())
                .orElseThrow(() -> new EntityNotFoundException("user_not_found"));
    }

}
