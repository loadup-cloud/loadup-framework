package com.github.loadup.modules.upms.repository;

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.modules.upms.domain.entity.Role;
import com.github.loadup.modules.upms.domain.gateway.RoleGateway;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Role Repository Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@DisplayName("RoleRepository Tests")
class RoleGatewayTest extends BaseRepositoryTest {

  @Autowired private RoleGateway roleGateway;

  private Role testRole;

  @BeforeEach
  void setUp() {
    testRole =
        Role.builder()
            .roleName("Test Role")
            .roleCode("TEST_ROLE")
            .roleLevel(1)
            .dataScope((short) 1)
            .sortOrder(0)
            .status((short) 1)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
  }

  @Test
  @DisplayName("Should save role successfully")
  void testSaveRole() {
    // When
    Role saved = roleGateway.save(testRole);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getRoleName()).isEqualTo("Test Role");
    assertThat(saved.getRoleCode()).isEqualTo("TEST_ROLE");
  }

  @Test
  @DisplayName("Should find role by code")
  void testFindByRoleCode() {
    // Given
    Role saved = roleGateway.save(testRole);

    // When
    var found = roleGateway.findByRoleCode("TEST_ROLE");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
  }

  @Test
  @DisplayName("Should check if role code exists")
  void testExistsByRoleCode() {
    // Given
    roleGateway.save(testRole);

    // When
    boolean exists = roleGateway.existsByRoleCode("TEST_ROLE");
    boolean notExists = roleGateway.existsByRoleCode("NONEXISTENT");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should find all roles")
  void testFindAll() {
    // Given
    roleGateway.save(testRole);
    Role role2 =
        Role.builder()
            .roleName("Role 2")
            .roleCode("ROLE_2")
            .roleLevel(1)
            .dataScope((short) 1)
            .sortOrder(1)
            .status((short) 1)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
    roleGateway.save(role2);

    // When
    List<Role> roles = roleGateway.findAll();

    // Then
    assertThat(roles).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("Should find enabled roles only")
  void testFindAllEnabled() {
    // Given
    roleGateway.save(testRole);
    Role disabledRole =
        Role.builder()
            .roleName("Disabled Role")
            .roleCode("DISABLED_ROLE")
            .roleLevel(1)
            .dataScope((short) 1)
            .sortOrder(1)
            .status((short) 0) // Disabled
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
    roleGateway.save(disabledRole);

    // When
    List<Role> roles = roleGateway.findAllEnabled();

    // Then
    assertThat(roles).isNotEmpty();
    assertThat(roles).allMatch(role -> role.getStatus() == 1);
  }

  @Test
  @DisplayName("Should support role hierarchy")
  void testRoleHierarchy() {
    // Given
    Role parentRole = roleGateway.save(testRole);
    Role childRole =
        Role.builder()
            .roleName("Child Role")
            .roleCode("CHILD_ROLE")
            .parentId(parentRole.getId())
            .roleLevel(2)
            .dataScope((short) 1)
            .sortOrder(1)
            .status((short) 1)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
    roleGateway.save(childRole);

    // When
    List<Role> children = roleGateway.findByParentId(parentRole.getId());

    // Then
    assertThat(children).hasSize(1);
    assertThat(children.get(0).getRoleCode()).isEqualTo("CHILD_ROLE");
  }

  @Test
  @DisplayName("Should update role")
  void testUpdateRole() {
    // Given
    Role saved = roleGateway.save(testRole);
    saved.setRoleName("Updated Role Name");
    saved.setDataScope((short) 2);

    // When
    Role updated = roleGateway.update(saved);

    // Then
    assertThat(updated.getRoleName()).isEqualTo("Updated Role Name");
    assertThat(updated.getDataScope()).isEqualTo((short) 2);
  }

  @Test
  @DisplayName("Should soft delete role")
  void testDeleteRole() {
    // Given
    Role saved = roleGateway.save(testRole);

    // When
    roleGateway.deleteById(saved.getId());

    // Then
    var found = roleGateway.findByRoleCode("TEST_ROLE");
    assertThat(found).isEmpty();
  }
}
