package com.navi.net_pay_backend.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PermissionAuthorizationManagerTest {
    private final PermissionAuthorizationManager permissions = new PermissionAuthorizationManager();

    @Test
    void mapsSpecialOperationsToTheirActualResourceAndAction() {
        assertEquals("reports:export", permissions.requiredPermission("GET", "/api/reports/collections/export"));
        assertEquals("receipts:export", permissions.requiredPermission("GET", "/api/payments/uuid/receipt/pdf"));
        assertEquals("receipts:read", permissions.requiredPermission("GET", "/api/payments/uuid/receipt"));
        assertEquals("payments:read", permissions.requiredPermission("GET", "/api/payments/uuid/attachment"));
        assertEquals("receipts:export", permissions.requiredPermission("POST", "/api/receipts/uuid/print"));
        assertEquals("clients:read", permissions.requiredPermission("GET", "/api/users/uuid/clients"));
        assertEquals("audit-logs:read", permissions.requiredPermission("GET", "/api/users/uuid/audit-logs"));
        assertEquals("clients:read", permissions.requiredPermission("GET", "/api/clients/uuid/payment-status"));
        assertEquals("audit-logs:read", permissions.requiredPermission("GET", "/api/audit-logs/user/uuid"));
        assertEquals("roles:update", permissions.requiredPermission("POST", "/api/users/uuid/roles"));
        assertEquals("roles:update", permissions.requiredPermission("DELETE", "/api/users/uuid/roles/assignment"));
        assertEquals("users:import", permissions.requiredPermission("POST", "/api/imports/users"));
        assertNull(permissions.requiredPermission("POST", "/api/imports/roles"));
        assertNull(permissions.requiredPermission("GET", "/api/new-unprotected-module"));
    }

    @Test
    void personalPagePermissionsAuthorizeTheExistingRoutesIndependentlyOfRoles() {
        assertTrue(allowed("GET", "/api/dashboard/collector", "my-dashboard:read"));
        assertFalse(allowed("GET", "/api/dashboard", "my-dashboard:read"));
        assertTrue(allowed("GET", "/api/notifications", "my-dashboard:read"));
        assertTrue(allowed("GET", "/api/notifications", "dashboard:read"));
        assertFalse(allowed("GET", "/api/notifications", "payments:read"));
        assertTrue(allowed("GET", "/api/clients", "my-clients:read"));
        assertTrue(allowed("GET", "/api/clients/uuid", "my-clients:read"));
        assertTrue(allowed("GET", "/api/clients/uuid/account", "my-clients:read"));
        assertTrue(allowed("GET", "/api/users/uuid/clients", "my-clients:read"));
        assertFalse(allowed("POST", "/api/clients", "my-clients:read"));
        assertTrue(allowed("GET", "/api/payments", "my-payments:read"));
        assertTrue(allowed("GET", "/api/payments/uuid", "my-payments:read"));
        assertTrue(allowed("GET", "/api/payments/uuid/attachment", "my-payments:read"));
        assertFalse(allowed("POST", "/api/payments/collector", "my-payments:read"));
        assertTrue(allowed("POST", "/api/payments/collector", "my-payments:create"));
        assertTrue(allowed("POST", "/api/files/upload", "any-existing-authority"));
        var unauthenticated = new RequestAuthorizationContext(new MockHttpServletRequest("POST", "/api/files/upload"));
        assertFalse(permissions.authorize(() -> null, unauthenticated).isGranted());
        assertFalse(allowed("POST", "/api/payments", "my-payments:create"));
        assertFalse(allowed("PUT", "/api/payments/uuid", "my-payments:create"));
        assertFalse(allowed("DELETE", "/api/payments/uuid", "my-payments:create"));
        assertTrue(allowed("GET", "/api/receipts", "my-receipts:read"));
        assertTrue(allowed("GET", "/api/payments/uuid/receipt", "my-receipts:read"));
        assertFalse(allowed("GET", "/api/payments/uuid/receipt/pdf", "my-receipts:read"));
        assertTrue(allowed("GET", "/api/payments", "payments:read"));
        assertTrue(allowed("POST", "/api/payments/collector", "payments:create"));
        assertFalse(allowed("GET", "/api/roles", "my-dashboard:read"));
    }

    @Test
    void roleUpdaterCanLoadRoleOptionsForUserImportWithoutRoleReadGrant() {
        assertTrue(allowed("GET", "/api/roles", "roles:update"));
        assertFalse(allowed("GET", "/api/roles/uuid", "roles:update"));
        assertFalse(allowed("GET", "/api/roles", "users:import"));
        assertTrue(allowed("GET", "/api/users/uuid/roles", "roles:update"));
        assertFalse(allowed("GET", "/api/users/uuid/roles", "users:update"));
    }

    @Test
    void personalReceiptExportAllowsPdfAndPrintingWithoutGrantingOtherExports() {
        assertTrue(allowed("GET", "/api/receipts/uuid/document", "my-receipts:export"));
        assertTrue(allowed("HEAD", "/api/receipts/uuid/document", "my-receipts:export"));
        assertTrue(allowed("GET", "/api/payments/uuid/receipt/pdf", "my-receipts:export"));
        assertTrue(allowed("POST", "/api/receipts/uuid/print", "my-receipts:export"));
        assertFalse(allowed("POST", "/api/receipts/uuid/print", "my-receipts:read"));
        assertFalse(allowed("GET", "/api/receipts/uuid/document", "my-receipts:read"));
        assertFalse(allowed("GET", "/api/reports/collections/export", "my-receipts:export"));
        assertFalse(allowed("DELETE", "/api/payments/uuid", "my-receipts:export"));
        assertTrue(allowed("POST", "/api/receipts/uuid/print", "receipts:export"));
    }

    @Test
    void typologiesAreAuthenticatedReadOnlyLookupsWithoutAnAssignablePermission() {
        assertTrue(allowed("GET", "/api/typologies/49"));
        assertTrue(allowed("GET", "/api/typologies/by-parent/48"));
        assertTrue(allowed("HEAD", "/api/typologies/49"));
        assertFalse(allowed("POST", "/api/typologies", "typologies:create"));
        assertFalse(allowed("PUT", "/api/typologies/49", "typologies:update"));
        assertFalse(allowed("DELETE", "/api/typologies/49", "typologies:delete"));
        var request = new RequestAuthorizationContext(new MockHttpServletRequest("GET", "/api/typologies/49"));
        assertFalse(permissions.authorize(() -> null, request).isGranted());
    }

    private boolean allowed(String method, String path, String... authorities) {
        var auth = new UsernamePasswordAuthenticationToken(1L, null,
                Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList());
        return permissions.authorize(() -> auth,
                new RequestAuthorizationContext(new MockHttpServletRequest(method, path))).isGranted();
    }
}
