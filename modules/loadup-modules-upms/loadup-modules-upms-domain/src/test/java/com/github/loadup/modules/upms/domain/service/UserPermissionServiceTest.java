package com.github.loadup.modules.upms.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.github.loadup.modules.upms.domain.entity.Permission;
import com.github.loadup.modules.upms.domain.entity.Role;
import com.github.loadup.modules.upms.domain.repository.PermissionRepository;
import com.github.loadup.modules.upms.domain.repository.RoleRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * UserPermissionService Unit Test
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("用户权限服务测试")
class UserPermissionServiceTest {

  @Mock private RoleRepository roleRepository;

  @Mock private PermissionRepository permissionRepository;

  @InjectMocks private UserPermissionService userPermissionService;

  private Role parentRole;
  private Role childRole;
  private Permission permission1;
  private Permission permission2;
  private Permission permission3;

  @BeforeEach
  void setUp() {
    // Setup parent role
    parentRole =
        Role.builder()
            .id(1L)
            .roleName("管理员")
            .roleCode("ROLE_ADMIN")
            .parentRoleId(null)
            .status((short) 1)
            .deleted(false)
            .build();

    // Setup child role (inherits from parent)
    childRole =
        Role.builder()
            .id(2L)
            .roleName("部门经理")
            .roleCode("ROLE_DEPT_MANAGER")
            .parentRoleId(1L)
            .status((short) 1)
            .deleted(false)
            .build();

    // Setup permissions
    permission1 =
        Permission.builder()
            .id(1L)
            .permissionCode("system:user:query")
            .permissionName("用户查询")
            .status((short) 1)
            .deleted(false)
            .build();

    permission2 =
        Permission.builder()
            .id(2L)
            .permissionCode("system:user:create")
            .permissionName("用户创建")
            .status((short) 1)
            .deleted(false)
            .build();

    permission3 =
        Permission.builder()
            .id(3L)
            .permissionCode("system:dept:query")
            .permissionName("部门查询")
            .status((short) 1)
            .deleted(false)
            .build();
  }

  @Test
  @DisplayName("测试获取用户权限")
  void testGetUserPermissions() {
    // Given
    Long userId = 100L;
    when(roleRepository.findByUserId(userId)).thenReturn(Arrays.asList(childRole));
    when(permissionRepository.findByRoleId(childRole.getId()))
        .thenReturn(Arrays.asList(permission3));
    when(roleRepository.findById(parentRole.getId())).thenReturn(Optional.of(parentRole));
    when(permissionRepository.findByRoleId(parentRole.getId()))
        .thenReturn(Arrays.asList(permission1, permission2));

    // When
    List<Permission> permissions = userPermissionService.getUserPermissions(userId);

    // Then
    assertNotNull(permissions);
    assertTrue(permissions.size() >= 3);
    verify(roleRepository).findByUserId(userId);
    verify(permissionRepository, atLeastOnce()).findByRoleId(anyLong());
  }

  @Test
  @DisplayName("测试检查用户是否有指定权限")
  void testHasPermission() {
    // Given
    Long userId = 100L;
    when(roleRepository.findByUserId(userId)).thenReturn(Arrays.asList(childRole));
    when(permissionRepository.findByRoleId(childRole.getId()))
        .thenReturn(Arrays.asList(permission1));
    when(roleRepository.findById(parentRole.getId())).thenReturn(Optional.empty());

    // When
    boolean hasPermission = userPermissionService.hasPermission(userId, "system:user:query");

    // Then
    assertTrue(hasPermission);
  }

  @Test
  @DisplayName("测试获取用户菜单权限")
  void testGetUserMenuPermissions() {
    // Given
    Long userId = 100L;
    Permission menuPermission =
        Permission.builder()
            .id(10L)
            .permissionCode("menu:system")
            .permissionName("系统管理")
            .permissionType((short) 1) // Menu type
            .visible(true)
            .sortOrder(1)
            .status((short) 1)
            .deleted(false)
            .build();

    when(roleRepository.findByUserId(userId)).thenReturn(Arrays.asList(childRole));
    when(permissionRepository.findByRoleId(childRole.getId()))
        .thenReturn(Arrays.asList(menuPermission));
    when(roleRepository.findById(anyLong())).thenReturn(Optional.empty());

    // When
    List<Permission> menuPermissions = userPermissionService.getUserMenuPermissions(userId);

    // Then
    assertNotNull(menuPermissions);
    assertTrue(menuPermissions.size() > 0);
    assertTrue(menuPermissions.stream().allMatch(Permission::isMenu));
  }
}
