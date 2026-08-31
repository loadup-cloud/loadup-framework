package io.github.loadup.modules.upms.infrastructure.security.datascope;

/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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

import io.github.loadup.modules.upms.domain.entity.Department;
import io.github.loadup.modules.upms.domain.entity.Role;
import io.github.loadup.modules.upms.domain.entity.User;
import io.github.loadup.modules.upms.domain.gateway.DepartmentGateway;
import io.github.loadup.modules.upms.domain.gateway.RoleGateway;
import io.github.loadup.modules.upms.domain.gateway.UserGateway;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Data Scope Aspect - Intercepts methods with @DataScope annotation
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Aspect
@Component
public class DataScopeAspect {
    private static final Logger log = LoggerFactory.getLogger(DataScopeAspect.class);

    private static final ThreadLocal<DataScopeContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final DepartmentGateway departmentGateway;

    /**
     * Get current data scope context
     */
    public static DataScopeContext getCurrentContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * Clear data scope context
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * Before method execution, build data scope context
     */
    @Before("@annotation(datascope.security.infrastructure.upms.modules.loadup.github.io.DataScope)")
    public void before(JoinPoint joinPoint) {
        try {
            // Get @DataScope annotation
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            DataScope dataScope = method.getAnnotation(DataScope.class);

            if (dataScope == null) {
                return;
            }

            // Get current authenticated user

            String username = "";
            User user = userGateway.findByUsername(username).orElse(null);
            if (user == null) {
                log.warn("User not found: {}", username);
                return;
            }

            // Build data scope context
            DataScopeContext context = buildDataScopeContext(user);
            CONTEXT_HOLDER.set(context);

            log.debug("Data scope context initialized for user: {}, scope: {}", username, context.getDataScopeType());

        } catch (Exception e) {
            log.error("Failed to initialize data scope context", e);
        }
    }

    /**
     * Build data scope context for user
     */
    private DataScopeContext buildDataScopeContext(User user) {
        // Get user's roles
        List<Role> roles = roleGateway.findByUserId(user.getId());

        // Check if user is super admin (has ADMIN role)
        boolean isSuperAdmin = roles.stream()
                .anyMatch(r -> "ROLE_ADMIN".equals(r.getRoleCode()) || "ROLE_SUPER_ADMIN".equals(r.getRoleCode()));

        // Find the most permissive data scope from all roles
        DataScopeType maxDataScope = DataScopeType.SELF; // Most restrictive by default
        List<String> customDeptIds = new ArrayList<>();

        for (Role role : roles) {
            if (role.getDataScope() != null) {
                DataScopeType roleScope = DataScopeType.fromCode(role.getDataScope());
                if (roleScope.getCode() < maxDataScope.getCode()) {
                    maxDataScope = roleScope;
                }

                // Collect custom department IDs
                if (roleScope == DataScopeType.CUSTOM) {
                    List<String> roleDeptIds = roleGateway.findDepartmentIdsByRoleId(role.getId());
                    customDeptIds.addAll(roleDeptIds);
                }
            }
        }

        // Get sub-departments if needed
        List<String> subDeptIds = new ArrayList<>();
        if (maxDataScope == DataScopeType.DEPT_AND_SUB && user.getDeptId() != null) {
            subDeptIds = getAllSubDepartmentIds(user.getDeptId());
        }

        return DataScopeContext.builder()
                .userId(user.getId())
                .deptId(user.getDeptId())
                .dataScopeType(maxDataScope)
                .customDeptIds(customDeptIds.stream().distinct().collect(Collectors.toList()))
                .subDeptIds(subDeptIds)
                .isSuperAdmin(isSuperAdmin)
                .build();
    }

    /**
     * Get all sub-department IDs recursively
     */
    private List<String> getAllSubDepartmentIds(String parentDeptId) {
        List<String> allIds = new ArrayList<>();
        List<Department> children = departmentGateway.findByParentId(parentDeptId);

        for (Department child : children) {
            allIds.add(child.getId());
            allIds.addAll(getAllSubDepartmentIds(child.getId()));
        }

        return allIds;
    }

    public DataScopeAspect(UserGateway userGateway, RoleGateway roleGateway, DepartmentGateway departmentGateway) {
        this.userGateway = userGateway;
        this.roleGateway = roleGateway;
        this.departmentGateway = departmentGateway;
    }
}
