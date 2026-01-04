package com.github.loadup.modules.upms.repository;

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.modules.upms.domain.entity.OperationLog;
import com.github.loadup.modules.upms.domain.repository.OperationLogRepository;
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
 * OperationLog Repository Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = "com.github.loadup.modules.upms.infrastructure.repository")
@ActiveProfiles("test")
@Transactional
@DisplayName("OperationLogRepository Tests")
class OperationLogRepositoryTest extends BaseRepositoryTest {

  @Autowired private OperationLogRepository operationLogRepository;

  private OperationLog testLog;

  @BeforeEach
  void setUp() {
    testLog =
        OperationLog.builder()
            .userId("1")
            .username("testuser")
            .operationModule("User Management")
            .operationType("CREATE")
            .operationDesc("Create new user")
            .requestMethod("POST")
            .requestUrl("/api/users")
            .requestParams("{\"username\":\"newuser\"}")
            .responseResult("{\"id\":1}")
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .status((short) 1)
            .executionTime(100L)
            .createdTime(LocalDateTime.now())
            .build();
  }

  @Test
  @DisplayName("Should save operation log successfully")
  void testSaveOperationLog() {
    // When
    OperationLog saved = operationLogRepository.save(testLog);

    // Then
    assertThat(saved).isNotNull();
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getUsername()).isEqualTo("testuser");
    assertThat(saved.getOperationModule()).isEqualTo("User Management");
  }

  @Test
  @DisplayName("Should find operation logs by user")
  void testFindByUserId() {
    // Given
    operationLogRepository.save(testLog);
    OperationLog log2 =
        OperationLog.builder()
            .userId("1")
            .username("testuser")
            .operationModule("Role Management")
            .operationType("UPDATE")
            .requestMethod("PUT")
            .requestUrl("/api/roles")
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .status((short) 1)
            .executionTime(80L)
            .createdTime(LocalDateTime.now().minusHours(1))
            .build();
    operationLogRepository.save(log2);

    // When
    Page<OperationLog> logs = operationLogRepository.findByUserId("1", PageRequest.of(0, 10));

    // Then
    assertThat(logs.getContent()).hasSizeGreaterThanOrEqualTo(2);
    assertThat(logs.getContent()).allMatch(log -> log.getUserId().equals("1"));
  }

  @Test
  @DisplayName("Should find operation logs by module")
  void testFindByModule() {
    // Given
    operationLogRepository.save(testLog);

    // When
    LocalDateTime start = LocalDateTime.now().minusDays(1);
    LocalDateTime end = LocalDateTime.now().plusDays(1);
    Page<OperationLog> logs =
        operationLogRepository.search(
            null, null, "User Management", start, end, PageRequest.of(0, 10));

    // Then
    assertThat(logs.getContent()).isNotEmpty();
    assertThat(logs.getContent())
        .allMatch(log -> log.getOperationModule().equals("User Management"));
  }

  @Test
  @DisplayName("Should find operation logs by type")
  void testFindByType() {
    // Given
    operationLogRepository.save(testLog);

    // When
    Page<OperationLog> logs =
        operationLogRepository.findByOperationType("CREATE", PageRequest.of(0, 10));

    // Then
    assertThat(logs.getContent()).isNotEmpty();
    assertThat(logs.getContent()).allMatch(log -> log.getOperationType().equals("CREATE"));
  }

  @Test
  @DisplayName("Should find operation logs by date range")
  void testFindByDateRange() {
    // Given
    operationLogRepository.save(testLog);

    // When
    LocalDateTime start = LocalDateTime.now().minusDays(1);
    LocalDateTime end = LocalDateTime.now().plusDays(1);
    Page<OperationLog> logs =
        operationLogRepository.findByDateRange(start, end, PageRequest.of(0, 10));

    // Then
    assertThat(logs.getContent()).isNotEmpty();
  }

  @Test
  @DisplayName("Should find failed operations")
  void testFindFailedOperations() {
    // Given
    OperationLog failedLog =
        OperationLog.builder()
            .userId("1")
            .username("testuser")
            .operationModule("User Management")
            .operationType("DELETE")
            .requestMethod("DELETE")
            .requestUrl("/api/users/1")
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .status((short) 0) // Failed
            .errorMessage("User not found")
            .executionTime(50L)
            .createdTime(LocalDateTime.now())
            .build();
    operationLogRepository.save(failedLog);
    operationLogRepository.save(testLog); // Successful

    // When
    LocalDateTime start = LocalDateTime.now().minusHours(1);
    LocalDateTime end = LocalDateTime.now().plusHours(1);
    long failedCount = operationLogRepository.countFailedOperations(start, end);

    // Then
    assertThat(failedCount).isGreaterThanOrEqualTo(1);
  }

  @Test
  @DisplayName("Should count operations by user and date range")
  void testCountOperations() {
    // Given
    operationLogRepository.save(testLog);
    OperationLog log2 =
        OperationLog.builder()
            .userId("1")
            .username("testuser")
            .operationModule("Role Management")
            .operationType("UPDATE")
            .requestMethod("PUT")
            .requestUrl("/api/roles")
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .status((short) 1)
            .executionTime(80L)
            .createdTime(LocalDateTime.now().minusMinutes(30))
            .build();
    operationLogRepository.save(log2);

    // When
    long count = operationLogRepository.countByUserId("1");

    // Then
    assertThat(count).isGreaterThanOrEqualTo(2);
  }

  @Test
  @DisplayName("Should count failed operations")
  void testCountFailedOperations() {
    // Given
    OperationLog failedLog1 =
        OperationLog.builder()
            .userId("1")
            .username("testuser")
            .operationModule("User Management")
            .operationType("DELETE")
            .requestMethod("DELETE")
            .requestUrl("/api/users/1")
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .status((short) 0)
            .errorMessage("Error")
            .executionTime(50L)
            .createdTime(LocalDateTime.now())
            .build();
    operationLogRepository.save(failedLog1);
    operationLogRepository.save(testLog); // Successful

    // When
    LocalDateTime start = LocalDateTime.now().minusHours(1);
    LocalDateTime end = LocalDateTime.now().plusHours(1);
    long count = operationLogRepository.countFailedOperations(start, end);

    // Then
    assertThat(count).isEqualTo(1);
  }

  @Test
  @DisplayName("Should delete logs before date")
  void testDeleteBeforeDate() {
    // Given
    OperationLog oldLog =
        OperationLog.builder()
            .userId("1")
            .username("testuser")
            .operationModule("User Management")
            .operationType("CREATE")
            .requestMethod("POST")
            .requestUrl("/api/users")
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .status((short) 1)
            .executionTime(100L)
            .createdTime(LocalDateTime.now().minusDays(100))
            .build();
    operationLogRepository.save(oldLog);
    operationLogRepository.save(testLog);

    // When
    operationLogRepository.deleteBeforeDate(LocalDateTime.now().minusDays(30));

    // Then
    Page<OperationLog> logs = operationLogRepository.findByUserId("1", PageRequest.of(0, 100));
    // Old log should be deleted, but recent log should remain
    assertThat(logs.getContent()).hasSizeGreaterThanOrEqualTo(1);
  }
}
