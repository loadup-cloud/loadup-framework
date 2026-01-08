package com.github.loadup.modules.upms.repository;

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.modules.upms.domain.entity.Permission;
import com.github.loadup.modules.upms.domain.gateway.PermissionGateway;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Permission Repository Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@DisplayName("PermissionRepository Tests")
class PermissionGatewayTest extends BaseRepositoryTest {

  @Autowired private PermissionGateway permissionGateway;

  private Permission testPermission;

  @BeforeEach
  void setUp() {
    testPermission =
        Permission.builder()
            .parentId("0")
            .permissionName("Test Permission")
            .permissionCode("TEST_PERM")
            .permissionType((short) 1) // Menu
            .resourcePath("/test")
            .httpMethod("GET")
            .sortOrder(0)
            .visible(true)
            .status((short) 1)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
  }

  @Test
  @DisplayName("Should save permission successfully")
  void testSavePermission() {
    // When
    Permission saved = permissionGateway.save(testPermission);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getPermissionName()).isEqualTo("Test Permission");
  }

  @Test
  @DisplayName("Should find permission by code")
  void testFindByPermissionCode() {
    // Given
    Permission saved = permissionGateway.save(testPermission);

    // When
    var found = permissionGateway.findByPermissionCode("TEST_PERM");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
  }

  @Test
  @DisplayName("Should check if permission code exists")
  void testExistsByPermissionCode() {
    // Given
    permissionGateway.save(testPermission);

    // When
    boolean exists = permissionGateway.existsByPermissionCode("TEST_PERM");
    boolean notExists = permissionGateway.existsByPermissionCode("NONEXISTENT");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should find all permissions")
  void testFindAll() {
    // Given
    permissionGateway.save(testPermission);
    Permission perm2 =
        Permission.builder()
            .parentId("0")
            .permissionName("Permission 2")
            .permissionCode("PERM_2")
            .permissionType((short) 2) // Button
            .sortOrder(1)
            .visible(true)
            .status((short) 1)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
    permissionGateway.save(perm2);

    // When
    List<Permission> permissions = permissionGateway.findAll();

    // Then
    assertThat(permissions).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("Should find menu permissions only")
  void testFindMenuPermissions() {
    // Given
    permissionGateway.save(testPermission); // Menu type
    Permission buttonPerm =
        Permission.builder()
            .parentId("0")
            .permissionName("Button Permission")
            .permissionCode("BUTTON_PERM")
            .permissionType((short) 2) // Button
            .sortOrder(1)
            .visible(true)
            .status((short) 1)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
    permissionGateway.save(buttonPerm);

    // When
    List<Permission> menuPerms = permissionGateway.findMenuPermissions();

    // Then
    assertThat(menuPerms).isNotEmpty();
    assertThat(menuPerms).allMatch(p -> p.getPermissionType() == 1);
  }

  @Test
  @DisplayName("Should find permissions by parent")
  void testFindByParentId() {
    // Given
    Permission parent = permissionGateway.save(testPermission);
    Permission child =
        Permission.builder()
            .parentId(parent.getId())
            .permissionName("Child Permission")
            .permissionCode("CHILD_PERM")
            .permissionType((short) 1)
            .sortOrder(0)
            .visible(true)
            .status((short) 1)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
    permissionGateway.save(child);

    // When
    List<Permission> children = permissionGateway.findByParentId(parent.getId());

    // Then
    assertThat(children).hasSize(1);
    assertThat(children.get(0).getPermissionCode()).isEqualTo("CHILD_PERM");
  }

  @Test
  @DisplayName("Should update permission")
  void testUpdatePermission() {
    // Given
    Permission saved = permissionGateway.save(testPermission);
    saved.setPermissionName("Updated Permission");
    saved.setResourcePath("/updated");

    // When
    Permission updated = permissionGateway.update(saved);

    // Then
    assertThat(updated.getPermissionName()).isEqualTo("Updated Permission");
    assertThat(updated.getResourcePath()).isEqualTo("/updated");
  }

  @Test
  @DisplayName("Should soft delete permission")
  void testDeletePermission() {
    // Given
    Permission saved = permissionGateway.save(testPermission);

    // When
    permissionGateway.deleteById(saved.getId());

    // Then
    var found = permissionGateway.findByPermissionCode("TEST_PERM");
    assertThat(found).isEmpty();
  }
}
