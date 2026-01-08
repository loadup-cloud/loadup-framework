package com.github.loadup.modules.upms.repository;

import static org.assertj.core.api.Assertions.*;

import com.github.loadup.modules.upms.domain.entity.OperationLog;
import com.github.loadup.modules.upms.domain.gateway.OperationLogGateway;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * OperationLog Repository Tests
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@DisplayName("OperationLogRepository Tests")
class OperationLogGatewayTest extends BaseRepositoryTest {

  @Autowired private OperationLogGateway operationLogGateway;

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
            .operationTime(LocalDateTime.now())
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
    OperationLog saved = operationLogGateway.save(testLog);

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
    operationLogGateway.save(testLog);
    OperationLog log2 =
        OperationLog.builder()
            .userId("1")
            .username("testuser")
            .operationModule("Role Management")
            .operationType("UPDATE")
            .requestMethod("PUT")
            .operationTime(LocalDateTime.now())
            .requestUrl("/api/roles")
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .status((short) 1)
            .executionTime(80L)
            .createdTime(LocalDateTime.now().minusHours(1))
            .build();
    operationLogGateway.save(log2);

    // When
    Page<OperationLog> logs = operationLogGateway.findByUserId("1", PageRequest.of(0, 10));

    // Then
    assertThat(logs.getContent()).hasSizeGreaterThanOrEqualTo(2);
    assertThat(logs.getContent()).allMatch(log -> log.getUserId().equals("1"));
  }

  @Test
  @DisplayName("Should find operation logs by module")
  void testFindByModule() {
    // Given
    operationLogGateway.save(testLog);

    // When
    LocalDateTime start = LocalDateTime.now().minusDays(1);
    LocalDateTime end = LocalDateTime.now().plusDays(1);
    Page<OperationLog> logs =
        operationLogGateway.search(
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
    operationLogGateway.save(testLog);

    // When
    Page<OperationLog> logs =
        operationLogGateway.findByOperationType("CREATE", PageRequest.of(0, 10));

    // Then
    assertThat(logs.getContent()).isNotEmpty();
    assertThat(logs.getContent()).allMatch(log -> log.getOperationType().equals("CREATE"));
  }

  @Test
  @DisplayName("Should find operation logs by date range")
  void testFindByDateRange() {
    // Given
    operationLogGateway.save(testLog);

    // When
    LocalDateTime start = LocalDateTime.now().minusDays(1);
    LocalDateTime end = LocalDateTime.now().plusDays(1);
    Page<OperationLog> logs =
        operationLogGateway.findByDateRange(start, end, PageRequest.of(0, 10));

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
            .operationTime(LocalDateTime.now())
            .createdTime(LocalDateTime.now())
            .build();
    operationLogGateway.save(failedLog);
    operationLogGateway.save(testLog); // Successful

    // When
    LocalDateTime start = LocalDateTime.now().minusHours(1);
    LocalDateTime end = LocalDateTime.now().plusHours(1);
    long failedCount = operationLogGateway.countFailedOperations(start, end);

    // Then
    assertThat(failedCount).isGreaterThanOrEqualTo(1);
  }

  @Test
  @DisplayName("Should count operations by user and date range")
  void testCountOperations() {
    // Given
    operationLogGateway.save(testLog);
    OperationLog log2 =
        OperationLog.builder()
            .userId("1")
            .username("testuser")
            .operationModule("Role Management")
            .operationType("UPDATE")
            .operationTime(LocalDateTime.now())
            .requestMethod("PUT")
            .requestUrl("/api/roles")
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .status((short) 1)
            .executionTime(80L)
            .createdTime(LocalDateTime.now().minusMinutes(30))
            .build();
    operationLogGateway.save(log2);

    // When
    long count = operationLogGateway.countByUserId("1");

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
            .operationTime(LocalDateTime.now())
            .executionTime(50L)
            .createdTime(LocalDateTime.now())
            .build();
    operationLogGateway.save(failedLog1);
    operationLogGateway.save(testLog); // Successful

    // When
    LocalDateTime start = LocalDateTime.now().minusHours(1);
    LocalDateTime end = LocalDateTime.now().plusHours(1);
    long count = operationLogGateway.countFailedOperations(start, end);

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
            .operationTime(LocalDateTime.now())
            .requestMethod("POST")
            .requestUrl("/api/users")
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .status((short) 1)
            .executionTime(100L)
            .createdTime(LocalDateTime.now().minusDays(100))
            .build();
    operationLogGateway.save(oldLog);
    operationLogGateway.save(testLog);

    // When
    operationLogGateway.deleteBeforeDate(LocalDateTime.now().minusDays(30));

    // Then
    Page<OperationLog> logs = operationLogGateway.findByUserId("1", PageRequest.of(0, 100));
    // Old log should be deleted, but recent log should remain
    assertThat(logs.getContent()).hasSizeGreaterThanOrEqualTo(1);
  }
}
