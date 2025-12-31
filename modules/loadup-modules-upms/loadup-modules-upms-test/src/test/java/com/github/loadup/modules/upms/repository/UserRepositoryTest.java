package com.github.loadup.modules.upms.repository;

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.modules.upms.domain.entity.User;
import com.github.loadup.modules.upms.domain.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Repository Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = "com.github.loadup.modules.upms.infrastructure.repository")
@ActiveProfiles("test")
@Transactional
@DisplayName("UserRepository Tests")
class UserRepositoryTest extends BaseRepositoryTest {

  @Autowired private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser =
        User.builder()
            .username("testuser")
            .password("$2a$10$encoded.password")
            .nickname("Test User")
            .realName("Test Real Name")
            .deptId(1L)
            .email("test@example.com")
            .emailVerified(false)
            .phone("13800138000")
            .phoneVerified(false)
            .gender((short) 1)
            .birthday(LocalDate.of(1990, 1, 1))
            .status((short) 1)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .loginFailCount(0)
            .deleted(false)
            .createdBy(1L)
            .createdTime(LocalDateTime.now())
            .build();
  }

  @Test
  @DisplayName("Should save user successfully")
  void testSaveUser() {
    // When
    User saved = userRepository.save(testUser);

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
    User saved = userRepository.save(testUser);

    // When
    var found = userRepository.findByUsername("testuser");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
    assertThat(found.get().getUsername()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("Should find user by email")
  void testFindByEmail() {
    // Given
    User saved = userRepository.save(testUser);

    // When
    var found = userRepository.findByEmail("test@example.com");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
    assertThat(found.get().getEmail()).isEqualTo("test@example.com");
  }

  @Test
  @DisplayName("Should find user by phone")
  void testFindByPhone() {
    // Given
    User saved = userRepository.save(testUser);

    // When
    var found = userRepository.findByPhone("13800138000");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
    assertThat(found.get().getPhone()).isEqualTo("13800138000");
  }

  @Test
  @DisplayName("Should check if username exists")
  void testExistsByUsername() {
    // Given
    userRepository.save(testUser);

    // When
    boolean exists = userRepository.existsByUsername("testuser");
    boolean notExists = userRepository.existsByUsername("nonexistent");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should check if email exists")
  void testExistsByEmail() {
    // Given
    userRepository.save(testUser);

    // When
    boolean exists = userRepository.existsByEmail("test@example.com");
    boolean notExists = userRepository.existsByEmail("nonexistent@example.com");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should check if phone exists")
  void testExistsByPhone() {
    // Given
    userRepository.save(testUser);

    // When
    boolean exists = userRepository.existsByPhone("13800138000");
    boolean notExists = userRepository.existsByPhone("13900139000");

    // Then
    assertThat(exists).isTrue();
    assertThat(notExists).isFalse();
  }

  @Test
  @DisplayName("Should update user")
  void testUpdateUser() {
    // Given
    User saved = userRepository.save(testUser);
    saved.setNickname("Updated Nickname");
    saved.setEmail("updated@example.com");

    // When
    User updated = userRepository.update(saved);

    // Then
    assertThat(updated.getNickname()).isEqualTo("Updated Nickname");
    assertThat(updated.getEmail()).isEqualTo("updated@example.com");
  }

  @Test
  @DisplayName("Should soft delete user")
  void testDeleteUser() {
    // Given
    User saved = userRepository.save(testUser);

    // When
    userRepository.deleteById(saved.getId());

    // Then - should not find deleted user
    var found = userRepository.findByUsername("testuser");
    assertThat(found).isEmpty();
  }

  @Test
  @DisplayName("Should find users by department")
  void testFindByDeptId() {
    // Given
    User user1 = userRepository.save(testUser);
    User user2 =
        User.builder()
            .username("testuser2")
            .password("$2a$10$encoded.password")
            .nickname("Test User 2")
            .deptId(1L)
            .status((short) 1)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .loginFailCount(0)
            .deleted(false)
            .createdBy(1L)
            .createdTime(LocalDateTime.now())
            .build();
    userRepository.save(user2);

    // When
    var users = userRepository.findByDeptId(1L);

    // Then
    assertThat(users).hasSize(2);
    assertThat(users).extracting(User::getUsername).contains("testuser", "testuser2");
  }

  @Test
  @DisplayName("Should search users by keyword")
  void testSearchByKeyword() {
    // Given
    userRepository.save(testUser);
    User user2 =
        User.builder()
            .username("another")
            .password("$2a$10$encoded.password")
            .nickname("Another User")
            .realName("Another Name")
            .deptId(1L)
            .status((short) 1)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .loginFailCount(0)
            .deleted(false)
            .createdBy(1L)
            .createdTime(LocalDateTime.now())
            .build();
    userRepository.save(user2);

    // When
    Page<User> results = userRepository.search("test", PageRequest.of(0, 10));

    // Then
    assertThat(results.getContent()).hasSizeLessThanOrEqualTo(1);
    assertThat(results.getTotalElements()).isGreaterThanOrEqualTo(1);
  }

  @Test
  @DisplayName("Should find all users with pagination")
  void testFindAllWithPagination() {
    // Given
    userRepository.save(testUser);
    for (int i = 1; i <= 5; i++) {
      User user =
          User.builder()
              .username("user" + i)
              .password("$2a$10$encoded.password")
              .nickname("User " + i)
              .deptId(1L)
              .status((short) 1)
              .accountNonExpired(true)
              .accountNonLocked(true)
              .credentialsNonExpired(true)
              .loginFailCount(0)
              .deleted(false)
              .createdBy(1L)
              .createdTime(LocalDateTime.now())
              .build();
      userRepository.save(user);
    }

    // When
    Page<User> page = userRepository.findAll(PageRequest.of(0, 3));

    // Then
    assertThat(page.getContent()).hasSizeLessThanOrEqualTo(3);
    assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(6);
  }

  @Test
  @DisplayName("Should count users by department")
  void testCountByDeptId() {
    // Given
    userRepository.save(testUser);
    User user2 =
        User.builder()
            .username("user2")
            .password("$2a$10$encoded.password")
            .nickname("User 2")
            .deptId(1L)
            .status((short) 1)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .loginFailCount(0)
            .deleted(false)
            .createdBy(1L)
            .createdTime(LocalDateTime.now())
            .build();
    userRepository.save(user2);

    // When
    long count = userRepository.countByDeptId(1L);

    // Then
    assertThat(count).isEqualTo(2);
  }
}
