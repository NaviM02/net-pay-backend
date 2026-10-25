package com.navi.net_pay_backend.domain.repository;

import com.navi.net_pay_backend.domain.entity.*;
import com.navi.net_pay_backend.domain.dto.PaginatedResult;
import com.navi.net_pay_backend.domain.dto.RoleQueryDto;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AccessControlRepository {
    List<Permission> permissions();
    List<Role> roles();
    PaginatedResult<Role> roles(RoleQueryDto query);
    Optional<Role> role(String hashId);
    Role saveRole(Role role, boolean create);
    void deleteRole(String hashId);
    List<UserRole> assignments(Long userId);
    UserRole assign(Long userId, String roleHashId, Instant startsAt, Instant endsAt);
    void cancel(Long userId, String assignmentHashId, Instant now);
    Set<String> effectivePermissions(Long userId, Instant now);
}
