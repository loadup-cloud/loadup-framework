package io.github.loadup.modules.upms.domain.service;

import io.github.loadup.modules.upms.domain.entity.Department;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.gateway.PermissionGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.valueobject.AccessDecision;
import io.github.loadup.modules.upms.domain.valueobject.DataScope;
import io.github.loadup.modules.upms.domain.valueobject.ResourceAttributes;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class AccessDecisionService {
    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final PermissionGateway permissionGateway;
    private final DepartmentGateway departmentGateway;

    public AccessDecisionService(
            UserGateway userGateway,
            RoleGateway roleGateway,
            PermissionGateway permissionGateway,
            DepartmentGateway departmentGateway) {
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
        this.permissionGateway = permissionGateway;
        this.departmentGateway = departmentGateway;
    }

    public AccessDecision decide(String userId, String permissionCode, ResourceAttributes resource) {
        if (userId == null || permissionCode == null || permissionCode.isBlank() || resource == null) {
            return AccessDecision.deny("INVALID_REQUEST");
        }
        User user = userGateway.findById(userId).orElse(null);
        if (user == null || !user.isActive()) return AccessDecision.deny("INACTIVE_SUBJECT");

        Set<String> visited = new HashSet<>();
        for (Role assigned : roleGateway.findByUserId(userId)) {
            Role role = assigned;
            while (role != null && role.getId() != null && visited.add(role.getId())) {
                if (!role.isEnabled()) break;
                boolean grantsPermission = permissionGateway.findByRoleId(role.getId()).stream()
                        .anyMatch(permission ->
                                permission.isEnabled() && permissionCode.equals(permission.getPermissionCode()));
                if (grantsPermission && matchesScope(role, user, resource)) {
                    return AccessDecision.permit(role.getRoleCode());
                }
                role = role.getParentId() == null
                        ? null
                        : roleGateway.findById(role.getParentId()).orElse(null);
            }
        }
        return AccessDecision.deny("NO_MATCHING_GRANT");
    }

    private boolean matchesScope(Role role, User user, ResourceAttributes resource) {
        return DataScope.fromCode(role.getDataScope())
                .map(scope -> switch (scope) {
                    case ALL -> true;
                    case OWNER ->
                        Objects.equals(user.getId(), resource.ownerUserId()) && resource.ownerUserId() != null;
                    case DEPARTMENT ->
                        Objects.equals(user.getDeptId(), resource.departmentId()) && resource.departmentId() != null;
                    case CUSTOM_DEPARTMENTS ->
                        resource.departmentId() != null
                                && roleGateway
                                        .findDepartmentIdsByRoleId(role.getId())
                                        .contains(resource.departmentId());
                    case DEPARTMENT_TREE -> isInDepartmentTree(user.getDeptId(), resource.departmentId());
                })
                .orElse(false);
    }

    private boolean isInDepartmentTree(String rootId, String resourceDeptId) {
        if (rootId == null || resourceDeptId == null) return false;
        Set<String> visited = new HashSet<>();
        String current = resourceDeptId;
        while (current != null && visited.add(current)) {
            if (rootId.equals(current)) return true;
            Department department = departmentGateway.findById(current).orElse(null);
            if (department == null || !department.isEnabled()) return false;
            current = department.getParentId();
        }
        return false;
    }
}
