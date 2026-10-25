package com.navi.net_pay_backend.infrastructure.security;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Supplier;

@Component
public class PermissionAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
            return new AuthorizationDecision(false);
        String path = context.getRequest().getServletPath();
        if (path.isEmpty()) path = context.getRequest().getRequestURI().substring(context.getRequest().getContextPath().length());
        if (Set.of("/api/users/me", "/api/auth/access").contains(path)) return new AuthorizationDecision(true);
        String method = context.getRequest().getMethod();
        if ("PUT".equals(method) && "/api/users/me/password".equals(path)) return new AuthorizationDecision(true);
        boolean read = Set.of("GET", "HEAD").contains(method);
        // Developer-managed lookup values are readable by any authenticated user.
        if (read && path.startsWith("/api/typologies/")) return new AuthorizationDecision(true);
        String permission = requiredPermission(context.getRequest().getMethod(), path);
        String pagePermission = personalPagePermission(method, path, permission);
        return new AuthorizationDecision(permission != null && auth.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(permission)
                        || authority.getAuthority().equals(pagePermission)));
    }

    private String personalPagePermission(String method, String path, String permission) {
        if ("POST".equals(method) && "/api/payments/collector".equals(path)) return "my-payments:create";
        if ("receipts:export".equals(permission)
                && (Set.of("GET", "HEAD").contains(method) || ("POST".equals(method) && path.endsWith("/print"))))
            return "my-receipts:export";
        if (!Set.of("GET", "HEAD").contains(method) || permission == null) return null;
        if ("/api/dashboard/collector".equals(path)) return "my-dashboard:read";
        return switch (permission) {
            case "clients:read" -> "my-clients:read";
            case "payments:read" -> "my-payments:read";
            case "receipts:read" -> "my-receipts:read";
            default -> null;
        };
    }

    // Explicit special operations precede ordinary HTTP CRUD. Unknown resources fail closed.
    public String requiredPermission(String method, String path) {
        String[] parts = path.split("/");
        if (parts.length < 3 || !"api".equals(parts[1])) return null;
        String resource = parts[2];
        if ("imports".equals(resource)) {
            return parts.length == 4 && "POST".equals(method)
                    && Set.of("users", "services", "clients", "payments").contains(parts[3]) ? parts[3] + ":import" : null;
        }
        if (!Set.of("users", "roles", "permissions", "services", "clients", "payments", "reports",
                "dashboard", "receipts", "audit-logs").contains(resource)) return null;
        if ("users".equals(resource) && parts.length >= 5) {
            if ("roles".equals(parts[4])) return "roles:" + ("GET".equals(method) ? "read" : "update");
            if ("clients".equals(parts[4])) resource = "clients";
            if ("audit-logs".equals(parts[4])) resource = "audit-logs";
        }
        if ("reports".equals(resource) && path.endsWith("/export")) return "reports:export";
        if ("receipts".equals(resource) && (path.endsWith("/document") || path.endsWith("/print"))) return "receipts:export";
        if ("payments".equals(resource) && path.endsWith("/receipt/pdf")) return "receipts:export";
        if ("payments".equals(resource) && path.endsWith("/receipt")) return "receipts:read";
        String action = switch (method) {
            case "GET", "HEAD" -> "read";
            case "POST" -> "create";
            case "PUT", "PATCH" -> "update";
            case "DELETE" -> "delete";
            default -> null;
        };
        return action == null ? null : resource + ":" + action;
    }
}
