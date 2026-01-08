package com.github.loadup.modules.upms.repository;

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.commons.util.RandomUtil;
import com.github.loadup.modules.upms.domain.entity.User;
import com.github.loadup.modules.upms.domain.gateway.UserGateway;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * User Repository Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@DisplayName("UserRepository Tests")
class UserGatewayTest extends BaseRepositoryTest {

  @Autowired private UserGateway userGateway;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .username("testuser")
            .password("$2a$10$encoded.password")
            .nickname("Test User")
            .realName("Test Real Name")
            .deptId("1")
            .email("test@example.com")
            .emailVerified(false)
            .mobile("1" + RandomUtil.randomNumbers(10))
            .mobileVerified(false)
            .gender((short) 1)
            .birthday(LocalDate.of(1990, 1, 1))
            .status((short) 1)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .loginFailCount(0)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
  }

  @Test
  @DisplayName("Should save user successfully")
  void testSaveUser() {
    // When
    User saved = userGateway.save(testUser);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getUsername()).isEqualTo("testuser");
    assertThat(saved.getEmail()).isEqualTo("test@example.com");
  }

  @Test
  @DisplayName("Should find user by username")
  void testFindByUsername() {
    // Given
    User saved = userGateway.save(testUser);

    // When
    var found = userGateway.findByUsername("testuser");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
    assertThat(found.get().getUsername()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("Should find user by email")
  void testFindByEmail() {
    // Given
    User saved = userGateway.save(testUser);

    // When
    var found = userGateway.findByEmail("test@example.com");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
    assertThat(found.get().getEmail()).isEqualTo("test@example.com");
  }

  @Test
  @DisplayName("Should find user by phone")
  void testFindByMobile() {
    // Given
    User saved = userGateway.save(testUser);

    // When
    var found = userGateway.findByMobile(testUser.getMobile());

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
    assertThat(found.get().getMobile()).isEqualTo(testUser.getMobile());
  }

  @Test
  @DisplayName("Should check if username exists")
  void testExistsByUsername() {
    // Given
    userGateway.save(testUser);

    // When
    boolean exists = userGateway.existsByUsername("testuser");
    boolean notExists = userGateway.existsByUsername("nonexistent");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should check if email exists")
  void testExistsByEmail() {
    // Given
    userGateway.save(testUser);

    // When
    boolean exists = userGateway.existsByEmail("test@example.com");
    boolean notExists = userGateway.existsByEmail("nonexistent@example.com");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should check if phone exists")
  void testExistsByMobile() {
    // Given
    userGateway.save(testUser);

    // When
    boolean exists = userGateway.existsByMobile(testUser.getMobile());
    boolean notExists = userGateway.existsByMobile("13900139000");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should update user")
  void testUpdateUser() {
    // Given
    User saved = userGateway.save(testUser);
    saved.setNickname("Updated Nickname");
    saved.setEmail("updated@example.com");

    // When
    User updated = userGateway.update(saved);

    // Then
    assertThat(updated.getNickname()).isEqualTo("Updated Nickname");
    assertThat(updated.getEmail()).isEqualTo("updated@example.com");
  }

  @Test
  @DisplayName("Should soft delete user")
  void testDeleteUser() {
    // Given
    User saved = userGateway.save(testUser);

    // When
    userGateway.deleteById(saved.getId());

    // Then - should not find deleted user
    var found = userGateway.findByUsername("testuser");
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should find users by department")
  void testFindByDeptId() {
    // Given
    testUser.setDeptId("100");
    userGateway.save(testUser);
    User user2 =
        User.builder()
            .username("testuser2")
            .password("$2a$10$encoded.password")
            .nickname("Test User 2")
            .deptId("100")
            .status((short) 1)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .loginFailCount(0)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
    userGateway.save(user2);

    // When
    var users = userGateway.findByDeptId("100");

    // Then
    assertThat(users).hasSize(2);
    assertThat(users).extracting(User::getUsername).contains("testuser", "testuser2");
  }

  @Test
  @DisplayName("Should search users by keyword")
  void testSearchByKeyword() {
    // Given
    userGateway.save(testUser);
    User user2 =
        User.builder()
            .username("another")
            .password("$2a$10$encoded.password")
            .nickname("Another User")
            .realName("Another Name")
            .deptId("1")
            .status((short) 1)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .loginFailCount(0)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
    userGateway.save(user2);

    // When
    Page<User> results = userGateway.search("test", PageRequest.of(0, 10));

    // Then
    assertThat(results.getContent()).hasSizeLessThanOrEqualTo(1);
    assertThat(results.getTotalElements()).isGreaterThanOrEqualTo(1);
  }

  @Test
  @DisplayName("Should find all users with pagination")
  void testFindAllWithPagination() {
    // Given
    userGateway.save(testUser);
    for (int i = 1; i <= 5; i++) {
      User user =
          User.builder()
              .username("user" + i)
              .password("$2a$10$encoded.password")
              .nickname("User " + i)
              .deptId("1")
              .status((short) 1)
              .accountNonExpired(true)
              .accountNonLocked(true)
              .credentialsNonExpired(true)
              .loginFailCount(0)
              .deleted(false)
              .createdBy("1")
              .createdTime(LocalDateTime.now())
              .build();
      userGateway.save(user);
    }

    // When
    Page<User> page = userGateway.findAll(PageRequest.of(0, 3));

    // Then
    assertThat(page.getContent()).hasSizeLessThanOrEqualTo(3);
    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(6);
  }

  @Test
  @DisplayName("Should count users by department")
  void testCountByDeptId() {
    // Given
    userGateway.save(testUser);
    User user2 =
        User.builder()
            .username("user2")
            .password("$2a$10$encoded.password")
            .nickname("User 2")
            .deptId("1")
            .status((short) 1)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .loginFailCount(0)
            .deleted(false)
            .createdBy("1")
            .createdTime(LocalDateTime.now())
            .build();
    userGateway.save(user2);

    // When
    long count = userGateway.countByDeptId("1");

    // Then
    assertThat(count).isEqualTo(2);
  }
}
