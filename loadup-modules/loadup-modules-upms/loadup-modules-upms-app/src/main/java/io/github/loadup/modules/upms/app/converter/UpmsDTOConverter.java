/*
 * #%L
 * Loadup Modules UPMS App Layer
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
package io.github.loadup.modules.upms.app.converter;

import io.github.loadup.commons.mapping.LoadUpMapStructConfig;
import io.github.loadup.modules.upms.client.dto.*;
import io.github.loadup.modules.upms.domain.entity.*;
import io.github.loadup.modules.upms.domain.valueobject.AccessDecision;
import java.util.List;
import org.mapstruct.*;

@Mapper(config = LoadUpMapStructConfig.class)
public interface UpmsDTOConverter {
    @Mapping(target = "extra", ignore = true)
    io.github.loadup.modules.upms.app.strategy.LoginCredentials toCredentials(
            io.github.loadup.modules.upms.client.command.UserLoginCommand command);

    @Mapping(target = "deptName", source = "departmentName")
    @Mapping(target = "roles", source = "roles")
    UserDetailDTO toUser(User user, String departmentName, List<RoleDTO> roles);

    @Mapping(target = "parentRoleName", source = "parentName")
    @Mapping(target = "permissions", source = "permissions")
    @Mapping(target = "departmentIds", source = "departments")
    @Mapping(target = "children", ignore = true)
    RoleDTO toRole(Role role, String parentName, List<PermissionDTO> permissions, List<String> departments);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "roleName", source = "roleName")
    @Mapping(target = "roleCode", source = "roleCode")
    @Mapping(target = "dataScope", source = "dataScope")
    @Mapping(target = "status", source = "status")
    RoleDTO toRoleSummary(Role role);

    @Mapping(target = "children", ignore = true)
    PermissionDTO toPermission(Permission permission);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "permissionName", source = "permissionName")
    @Mapping(target = "permissionCode", source = "permissionCode")
    @Mapping(target = "permissionType", source = "permissionType")
    PermissionDTO toPermissionSummary(Permission permission);

    @Mapping(target = "leaderUserName", source = "leaderName")
    @Mapping(target = "children", ignore = true)
    DepartmentDTO toDepartment(Department department, String leaderName);

    @Mapping(target = "userId", source = "id")
    @Mapping(target = "accountNonLocked", expression = "java(Boolean.TRUE.equals(user.getAccountNonLocked()))")
    @Mapping(target = "loginFailCount", defaultValue = "0")
    @Mapping(target = "passwordUpdatedAt", source = "passwordUpdateTime")
    @Mapping(target = "lastLoginAt", source = "lastLoginTime")
    SecurityOverviewDTO toSecurityOverview(User user);

    @Mapping(target = "loginAt", source = "loginTime")
    LoginEntryDTO toLoginEntry(LoginLog log);

    AccessDecisionDTO toAccessDecision(AccessDecision decision);
}
