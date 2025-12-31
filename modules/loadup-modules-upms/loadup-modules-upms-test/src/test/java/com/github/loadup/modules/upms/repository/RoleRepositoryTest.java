package com.github.loadup.modules.upms.repository;

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.modules.upms.domain.entity.Role;
import com.github.loadup.modules.upms.domain.repository.RoleRepository;
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
 * Role Repository Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = "com.github.loadup.modules.upms.infrastructure.repository")
@ActiveProfiles("test")
@Transactional
@DisplayName("RoleRepository Tests")
class RoleRepositoryTest extends BaseRepositoryTest {

  @Autowired private RoleRepository roleRepository;

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
            .createdBy(1L)
            .createdTime(LocalDateTime.now())
            .build();
  }

  @Test
  @DisplayName("Should save role successfully")
  void testSaveRole() {
    // When
    Role saved = roleRepository.save(testRole);

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
    Role saved = roleRepository.save(testRole);

    // When
    var found = roleRepository.findByRoleCode("TEST_ROLE");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
  }

  @Test
  @DisplayName("Should check if role code exists")
  void testExistsByRoleCode() {
    // Given
    roleRepository.save(testRole);

    // When
    boolean exists = roleRepository.existsByRoleCode("TEST_ROLE");
    boolean notExists = roleRepository.existsByRoleCode("NONEXISTENT");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should find all roles")
  void testFindAll() {
    // Given
    roleRepository.save(testRole);
    Role role2 =
        Role.builder()
            .roleName("Role 2")
            .roleCode("ROLE_2")
            .roleLevel(1)
            .dataScope((short) 1)
            .sortOrder(1)
            .status((short) 1)
            .deleted(false)
            .createdBy(1L)
            .createdTime(LocalDateTime.now())
            .build();
    roleRepository.save(role2);

    // When
    List<Role> roles = roleRepository.findAll();

    // Then
    assertThat(roles).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("Should find enabled roles only")
  void testFindAllEnabled() {
    // Given
    roleRepository.save(testRole);
    Role disabledRole =
        Role.builder()
            .roleName("Disabled Role")
            .roleCode("DISABLED_ROLE")
            .roleLevel(1)
            .dataScope((short) 1)
            .sortOrder(1)
            .status((short) 0) // Disabled
            .deleted(false)
            .createdBy(1L)
            .createdTime(LocalDateTime.now())
            .build();
    roleRepository.save(disabledRole);

    // When
    List<Role> roles = roleRepository.findAllEnabled();

    // Then
    assertThat(roles).isNotEmpty();
    assertThat(roles).allMatch(role -> role.getStatus() == 1);
  }

  @Test
  @DisplayName("Should support role hierarchy")
  void testRoleHierarchy() {
    // Given
    Role parentRole = roleRepository.save(testRole);
    Role childRole =
        Role.builder()
            .roleName("Child Role")
            .roleCode("CHILD_ROLE")
            .parentRoleId(parentRole.getId())
            .roleLevel(2)
            .dataScope((short) 1)
            .sortOrder(1)
            .status((short) 1)
            .deleted(false)
            .createdBy(1L)
            .createdTime(LocalDateTime.now())
            .build();
    roleRepository.save(childRole);

    // When
    List<Role> children = roleRepository.findByParentRoleId(parentRole.getId());

    // Then
    assertThat(children).hasSize(1);
    assertThat(children.get(0).getRoleCode()).isEqualTo("CHILD_ROLE");
  }

  @Test
  @DisplayName("Should update role")
  void testUpdateRole() {
    // Given
    Role saved = roleRepository.save(testRole);
    saved.setRoleName("Updated Role Name");
    saved.setDataScope((short) 2);

    // When
    Role updated = roleRepository.update(saved);

    // Then
    assertThat(updated.getRoleName()).isEqualTo("Updated Role Name");
    assertThat(updated.getDataScope()).isEqualTo((short) 2);
  }

  @Test
  @DisplayName("Should soft delete role")
  void testDeleteRole() {
    // Given
    Role saved = roleRepository.save(testRole);

    // When
    roleRepository.deleteById(saved.getId());

    // Then
    var found = roleRepository.findByRoleCode("TEST_ROLE");
    assertThat(found).isEmpty();
  }
}
