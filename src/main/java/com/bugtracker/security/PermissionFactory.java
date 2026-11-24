package com.bugtracker.security;

import com.bugtracker.model.Role;

/**
 * Gives back the right permission object for a role.
 * The caller only knows the interface "RolePermissions" - it does not care which class it got.
 */
public class PermissionFactory {

    private PermissionFactory() {
    }

    public static RolePermissions forRole(Role role) {
        switch (role) {
            case ADMIN:
                return new AdminPermissions();
            case DEVELOPER:
                return new DeveloperPermissions();
            case TESTER:
                return new TesterPermissions();
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}
