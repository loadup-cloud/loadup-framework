package io.github.loadup.modules.upms.domain.service;

import io.github.loadup.modules.upms.domain.entity.Permission;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.gateway.PermissionGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User Permission Domain Service Handles complex permission calculation logic including role
 * inheritance
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class UserPermissionService {

    private final RoleGateway roleGateway;
    private final PermissionGateway permissionGateway;

    public List<Permission> getUserPermissions(String userId) {
        Map<String, Permission> granted = new LinkedHashMap<>();
        for (Role assigned : roleGateway.findByUserId(userId)) {
            Set<String> visited = new HashSet<>();
            Role role = assigned;
            while (role != null && role.getId() != null && visited.add(role.getId())) {
                if (!role.isEnabled()) break;
                for (Permission permission : permissionGateway.findByRoleId(role.getId())) {
                    if (permission.isEnabled()) granted.put(permission.getId(), permission);
                }
                role = role.getParentId() == null
                        ? null
                        : roleGateway.findById(role.getParentId()).orElse(null);
            }
        }
        return new ArrayList<>(granted.values());
    }

    /**
     * Get permission codes for a user
     */
    public Set<String> getUserPermissionCodes(String userId) {
        return getUserPermissions(userId).stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toSet());
    }

    /**
     * Check if user has specific permission
     */
    public boolean hasPermission(String userId, String permissionCode) {
        return getUserPermissionCodes(userId).contains(permissionCode);
    }

    /**
     * Check if user has any of the specified permissions
     */
    public boolean hasAnyPermission(String userId, String... permissionCodes) {
        Set<String> userPermissions = getUserPermissionCodes(userId);
        for (String code : permissionCodes) {
            if (userPermissions.contains(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user has all of the specified permissions
     */
    public boolean hasAllPermissions(String userId, String... permissionCodes) {
        Set<String> userPermissions = getUserPermissionCodes(userId);
        for (String code : permissionCodes) {
            if (!userPermissions.contains(code)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get menu permissions for a user
     */
    public List<Permission> getUserMenuPermissions(String userId) {
        return getUserPermissions(userId).stream()
                .filter(Permission::isMenu)
                .filter(p -> Boolean.TRUE.equals(p.getVisible()))
                .sorted((p1, p2) -> {
                    int order1 = p1.getSortOrder() != null ? p1.getSortOrder() : 0;
                    int order2 = p2.getSortOrder() != null ? p2.getSortOrder() : 0;
                    return Integer.compare(order1, order2);
                })
                .collect(Collectors.toList());
    }

    public UserPermissionService(RoleGateway roleGateway, PermissionGateway permissionGateway) {
        this.roleGateway = roleGateway;
        this.permissionGateway = permissionGateway;
    }
}
