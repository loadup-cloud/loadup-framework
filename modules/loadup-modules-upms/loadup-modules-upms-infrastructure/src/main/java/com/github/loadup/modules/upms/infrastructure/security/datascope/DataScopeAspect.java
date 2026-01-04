package com.github.loadup.modules.upms.infrastructure.security.datascope;

import com.github.loadup.modules.upms.domain.entity.Department;
import com.github.loadup.modules.upms.domain.entity.Role;
import com.github.loadup.modules.upms.domain.entity.User;
import com.github.loadup.modules.upms.domain.repository.DepartmentRepository;
import com.github.loadup.modules.upms.domain.repository.RoleRepository;
import com.github.loadup.modules.upms.domain.repository.UserRepository;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Data Scope Aspect - Intercepts methods with @DataScope annotation
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeAspect {

  private static final ThreadLocal<DataScopeContext> CONTEXT_HOLDER = new ThreadLocal<>();

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final DepartmentRepository departmentRepository;

  /** Get current data scope context */
  public static DataScopeContext getCurrentContext() {
    return CONTEXT_HOLDER.get();
  }

  /** Clear data scope context */
  public static void clearContext() {
    CONTEXT_HOLDER.remove();
  }

  /** Before method execution, build data scope context */
  @Before("@annotation(com.github.loadup.modules.upms.infrastructure.security.datascope.DataScope)")
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
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null || !authentication.isAuthenticated()) {
        log.debug("No authenticated user, skipping data scope");
        return;
      }

      String username = authentication.getName();
      User user = userRepository.findByUsername(username).orElse(null);
      if (user == null) {
        log.warn("User not found: {}", username);
        return;
      }

      // Build data scope context
      DataScopeContext context = buildDataScopeContext(user);
      CONTEXT_HOLDER.set(context);

      log.debug(
          "Data scope context initialized for user: {}, scope: {}",
          username,
          context.getDataScopeType());

    } catch (Exception e) {
      log.error("Failed to initialize data scope context", e);
    }
  }

  /** Build data scope context for user */
  private DataScopeContext buildDataScopeContext(User user) {
    // Get user's roles
    List<Role> roles = roleRepository.findByUserId(user.getId());

    // Check if user is super admin (has ADMIN role)
    boolean isSuperAdmin =
        roles.stream()
            .anyMatch(
                r ->
                    "ROLE_ADMIN".equals(r.getRoleCode())
                        || "ROLE_SUPER_ADMIN".equals(r.getRoleCode()));

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
          List<String> roleDeptIds = roleRepository.findDepartmentIdsByRoleId(role.getId());
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

  /** Get all sub-department IDs recursively */
  private List<String> getAllSubDepartmentIds(String parentDeptId) {
    List<String> allIds = new ArrayList<>();
    List<Department> children = departmentRepository.findByParentId(parentDeptId);

    for (Department child : children) {
      allIds.add(child.getId());
      allIds.addAll(getAllSubDepartmentIds(child.getId()));
    }

    return allIds;
  }
}
