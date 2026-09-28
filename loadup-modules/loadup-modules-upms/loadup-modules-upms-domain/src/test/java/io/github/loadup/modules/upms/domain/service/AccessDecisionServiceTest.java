/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.modules.upms.domain.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.loadup.modules.upms.domain.entity.Department;
import io.github.loadup.modules.upms.domain.entity.Permission;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.gateway.PermissionGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import io.github.loadup.modules.upms.domain.valueobject.DataScope;
import io.github.loadup.modules.upms.domain.valueobject.ResourceAttributes;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccessDecisionServiceTest {
    private final UserGateway users = mock(UserGateway.class);
    private final RoleGateway roles = mock(RoleGateway.class);
    private final PermissionGateway permissions = mock(PermissionGateway.class);
    private final DepartmentGateway departments = mock(DepartmentGateway.class);
    private final AccessDecisionService service = new AccessDecisionService(users, roles, permissions, departments);

    @BeforeEach
    void subject() {
        User user = new User();
        user.setId("user-1");
        user.setDeptId("dept-1");
        user.setStatus((short) 1);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);
        when(users.findById("user-1")).thenReturn(Optional.of(user));
    }

    @Test
    void ownerScopeRequiresBothGrantAndMatchingOwner() {
        Role role = role("role-1", null, DataScope.OWNER);
        when(roles.findByUserId("user-1")).thenReturn(List.of(role));
        when(permissions.findByRoleId("role-1")).thenReturn(List.of(permission("document:read")));

        assertTrue(service.decide("user-1", "document:read", new ResourceAttributes("user-1", null))
                .allowed());
        assertFalse(service.decide("user-1", "document:read", new ResourceAttributes("other", null))
                .allowed());
        assertFalse(service.decide("user-1", "document:write", new ResourceAttributes("user-1", null))
                .allowed());
    }

    @Test
    void inheritedPermissionUsesGrantingParentsScope() {
        Role child = role("child", "parent", DataScope.ALL);
        Role parent = role("parent", "child", DataScope.OWNER);
        when(roles.findByUserId("user-1")).thenReturn(List.of(child));
        when(roles.findById("parent")).thenReturn(Optional.of(parent));
        when(roles.findById("child")).thenReturn(Optional.of(child));
        when(permissions.findByRoleId("parent")).thenReturn(List.of(permission("document:read")));

        assertFalse(service.decide("user-1", "document:read", new ResourceAttributes("other", null))
                .allowed());
        assertTrue(service.decide("user-1", "document:read", new ResourceAttributes("user-1", null))
                .allowed());
    }

    @Test
    void departmentTreeAllowsDescendantsOnly() {
        Role role = role("role-1", null, DataScope.DEPARTMENT_TREE);
        Department child = new Department();
        child.setId("dept-2");
        child.setParentId("dept-1");
        child.setStatus((short) 1);
        when(roles.findByUserId("user-1")).thenReturn(List.of(role));
        when(permissions.findByRoleId("role-1")).thenReturn(List.of(permission("document:read")));
        when(departments.findById("dept-2")).thenReturn(Optional.of(child));

        assertTrue(service.decide("user-1", "document:read", new ResourceAttributes(null, "dept-2"))
                .allowed());
        assertFalse(service.decide("user-1", "document:read", new ResourceAttributes(null, "other"))
                .allowed());
    }

    private static Role role(String id, String parentId, DataScope scope) {
        Role role = new Role();
        role.setId(id);
        role.setRoleCode(id);
        role.setParentId(parentId);
        role.setDataScope(scope.code());
        role.setStatus((short) 1);
        return role;
    }

    private static Permission permission(String code) {
        Permission permission = new Permission();
        permission.setId(code);
        permission.setPermissionCode(code);
        permission.setStatus((short) 1);
        return permission;
    }
}
