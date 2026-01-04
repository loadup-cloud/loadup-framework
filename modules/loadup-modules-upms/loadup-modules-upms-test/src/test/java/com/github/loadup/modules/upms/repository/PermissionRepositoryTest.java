package com.github.loadup.modules.upms.repository;

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.modules.upms.domain.entity.Permission;
import com.github.loadup.modules.upms.domain.repository.PermissionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Permission Repository Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = "com.github.loadup.modules.upms.infrastructure.repository")
@ActiveProfiles("test")
@Transactional
@DisplayName("PermissionRepository Tests")
class PermissionRepositoryTest extends BaseRepositoryTest {

  @Autowired private PermissionRepository permissionRepository;

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
    Permission saved = permissionRepository.save(testPermission);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getPermissionName()).isEqualTo("Test Permission");
  }

  @Test
  @DisplayName("Should find permission by code")
  void testFindByPermissionCode() {
    // Given
    Permission saved = permissionRepository.save(testPermission);

    // When
    var found = permissionRepository.findByPermissionCode("TEST_PERM");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
  }

  @Test
  @DisplayName("Should check if permission code exists")
  void testExistsByPermissionCode() {
    // Given
    permissionRepository.save(testPermission);

    // When
    boolean exists = permissionRepository.existsByPermissionCode("TEST_PERM");
    boolean notExists = permissionRepository.existsByPermissionCode("NONEXISTENT");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should find all permissions")
  void testFindAll() {
    // Given
    permissionRepository.save(testPermission);
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
    permissionRepository.save(perm2);

    // When
    List<Permission> permissions = permissionRepository.findAll();

    // Then
    assertThat(permissions).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("Should find menu permissions only")
  void testFindMenuPermissions() {
    // Given
    permissionRepository.save(testPermission); // Menu type
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
    permissionRepository.save(buttonPerm);

    // When
    List<Permission> menuPerms = permissionRepository.findMenuPermissions();

    // Then
    assertThat(menuPerms).isNotEmpty();
    assertThat(menuPerms).allMatch(p -> p.getPermissionType() == 1);
  }

  @Test
  @DisplayName("Should find permissions by parent")
  void testFindByParentId() {
    // Given
    Permission parent = permissionRepository.save(testPermission);
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
    permissionRepository.save(child);

    // When
    List<Permission> children = permissionRepository.findByParentId(parent.getId());

    // Then
    assertThat(children).hasSize(1);
    assertThat(children.get(0).getPermissionCode()).isEqualTo("CHILD_PERM");
  }

  @Test
  @DisplayName("Should update permission")
  void testUpdatePermission() {
    // Given
    Permission saved = permissionRepository.save(testPermission);
    saved.setPermissionName("Updated Permission");
    saved.setResourcePath("/updated");

    // When
    Permission updated = permissionRepository.update(saved);

    // Then
    assertThat(updated.getPermissionName()).isEqualTo("Updated Permission");
    assertThat(updated.getResourcePath()).isEqualTo("/updated");
  }

  @Test
  @DisplayName("Should soft delete permission")
  void testDeletePermission() {
    // Given
    Permission saved = permissionRepository.save(testPermission);

    // When
    permissionRepository.deleteById(saved.getId());

    // Then
    var found = permissionRepository.findByPermissionCode("TEST_PERM");
    assertThat(found).isEmpty();
  }
}
