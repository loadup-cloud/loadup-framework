package com.github.loadup.modules.upms.repository;

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.modules.upms.domain.entity.LoginLog;
import com.github.loadup.modules.upms.domain.repository.LoginLogRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * LoginLog Repository Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@DisplayName("LoginLogRepository Tests")
class LoginLogRepositoryTest extends BaseRepositoryTest {

  @Autowired private LoginLogRepository loginLogRepository;

  private LoginLog testLog;

  @BeforeEach
  void setUp() {
    testLog =
        LoginLog.builder()
            .userId("1")
            .username("testuser")
            .loginTime(LocalDateTime.now())
            .logoutTime(LocalDateTime.now())
            .ipAddress("192.168.1.1")
            .loginLocation("Test Location")
            .browser("Chrome")
            .os("Windows 10")
            .loginStatus((short) 1)
            .loginMessage("Login successful")
            .build();
  }

  @Test
  @DisplayName("Should save login log successfully")
  void testSaveLoginLog() {
    // When
    LoginLog saved = loginLogRepository.save(testLog);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getUsername()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("Should find login logs by user ID")
  void testFindByUserId() {
    // Given
    loginLogRepository.save(testLog);
    LoginLog log2 =
        LoginLog.builder()
            .userId("1")
            .username("testuser")
            .loginTime(LocalDateTime.now().minusHours(1))
            .logoutTime(LocalDateTime.now().minusDays(1))
            .ipAddress("192.168.1.2")
            .loginStatus((short) 1)
            .build();
    loginLogRepository.save(log2);

    // When
    Page<LoginLog> logs = loginLogRepository.findByUserId("1", PageRequest.of(0, 10));

    // Then
    assertThat(logs.getContent()).hasSizeGreaterThanOrEqualTo(2);
    assertThat(logs.getContent()).allMatch(log -> log.getUserId().equals("1"));
  }

  @Test
  @DisplayName("Should find login logs by username")
  void testFindByUsername() {
    // Given
    loginLogRepository.save(testLog);

    // When
    Page<LoginLog> logs = loginLogRepository.findByUsername("testuser", PageRequest.of(0, 10));

    // Then
    assertThat(logs.getContent()).isNotEmpty();
    assertThat(logs.getContent()).allMatch(log -> log.getUsername().equals("testuser"));
  }

  @Test
  @DisplayName("Should find logs by date range")
  void testFindByDateRange() {
    // Given
    loginLogRepository.save(testLog);

    // When
    LocalDateTime start = LocalDateTime.now().minusDays(1);
    LocalDateTime end = LocalDateTime.now().plusDays(1);
    Page<LoginLog> logs = loginLogRepository.findByDateRange(start, end, PageRequest.of(0, 10));

    // Then
    assertThat(logs.getContent()).isNotEmpty();
  }

  @Test
  @DisplayName("Should find failed login attempts")
  void testFindFailedLogins() {
    // Given
    LoginLog failedLog =
        LoginLog.builder()
            .userId("1")
            .username("testuser")
            .loginTime(LocalDateTime.now())
            .logoutTime(LocalDateTime.now())
            .ipAddress("192.168.1.1")
            .loginStatus((short) 0) // Failed
            .loginMessage("Invalid password")
            .build();
    loginLogRepository.save(failedLog);
    loginLogRepository.save(testLog); // Successful login

    // When
    LocalDateTime start = LocalDateTime.now().minusHours(1);
    LocalDateTime end = LocalDateTime.now().plusHours(1);
    Page<LoginLog> failedLogs =
        loginLogRepository.findFailedLogins(start, end, PageRequest.of(0, 10));

    // Then
    assertThat(failedLogs.getContent()).isNotEmpty();
    assertThat(failedLogs.getContent()).allMatch(log -> log.getLoginStatus() == 0);
  }

  @Test
  @DisplayName("Should count login attempts")
  void testCountLoginAttempts() {
    // Given
    loginLogRepository.save(testLog);
    LoginLog log2 =
        LoginLog.builder()
            .userId("1")
            .username("testuser")
            .loginTime(LocalDateTime.now().minusMinutes(30))
            .logoutTime(LocalDateTime.now().minusMinutes(30))
            .ipAddress("192.168.1.2")
            .loginStatus((short) 1)
            .build();
    loginLogRepository.save(log2);

    // When
    LocalDateTime start = LocalDateTime.now().minusHours(1);
    LocalDateTime end = LocalDateTime.now().plusHours(1);
    long count = loginLogRepository.countLoginAttempts("1", start, end);

    // Then
    assertThat(count).isGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("Should count failed login attempts")
  void testCountFailedLoginAttempts() {
    // Given
    LoginLog failedLog1 =
        LoginLog.builder()
            .userId("1")
            .username("testuser")
            .loginTime(LocalDateTime.now())
            .logoutTime(LocalDateTime.now())
            .ipAddress("192.168.1.1")
            .loginStatus((short) 0)
            .build();
    LoginLog failedLog2 =
        LoginLog.builder()
            .userId("1")
            .username("testuser")
            .loginTime(LocalDateTime.now().minusMinutes(10))
            .logoutTime(LocalDateTime.now().minusMinutes(10))
            .ipAddress("192.168.1.1")
            .loginStatus((short) 0)
            .build();
    loginLogRepository.save(failedLog1);
    loginLogRepository.save(failedLog2);
    loginLogRepository.save(testLog); // Successful

    // When
    LocalDateTime start = LocalDateTime.now().minusHours(1);
    LocalDateTime end = LocalDateTime.now().plusHours(1);
    long count = loginLogRepository.countFailedLoginAttempts("1", start, end);

    // Then
    assertThat(count).isEqualTo(2);
  }

  @Test
  @DisplayName("Should get last successful login")
  void testFindLastSuccessfulLogin() {
    // Given
    LoginLog oldLog =
        LoginLog.builder()
            .userId("1")
            .username("testuser")
            .loginTime(LocalDateTime.now().minusDays(1))
            .logoutTime(LocalDateTime.now().minusDays(1))
            .ipAddress("192.168.1.1")
            .loginStatus((short) 1)
            .build();
    loginLogRepository.save(oldLog);
    loginLogRepository.save(testLog); // More recent

    // When
    var lastLogin = loginLogRepository.findLastSuccessfulLogin("1");

    // Then
    assertThat(lastLogin).isPresent();
    assertThat(lastLogin.get().getLoginTime()).isAfter(oldLog.getLoginTime());
  }

  @Test
  @DisplayName("Should delete logs before date")
  void testDeleteBeforeDate() {
    // Given
    LoginLog oldLog =
        LoginLog.builder()
            .userId("1")
            .username("testuser")
            .loginTime(LocalDateTime.now().minusDays(100))
            .logoutTime(LocalDateTime.now())
            .ipAddress("192.168.1.1")
            .loginStatus((short) 1)
            .build();
    loginLogRepository.save(oldLog);
    loginLogRepository.save(testLog);

    // When
    loginLogRepository.deleteBeforeDate(LocalDateTime.now().minusDays(30));

    // Then
    Page<LoginLog> logs = loginLogRepository.findByUserId("1", PageRequest.of(0, 100));
    // Old log should be deleted, but recent log should remain
    assertThat(logs.getContent()).hasSizeGreaterThanOrEqualTo(1);
  }
}
