package com.github.loadup.modules.upms.domain.repository;

import com.github.loadup.modules.upms.domain.entity.OperationLog;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Operation Log Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface OperationLogRepository {

  /** Save operation log */
  OperationLog save(OperationLog log);

  /** Batch save operation logs */
  void batchSave(List<OperationLog> logs);

  /** Find log by ID */
  Optional<OperationLog> findById(Long id);

  /** Find logs by user ID */
  Page<OperationLog> findByUserId(Long userId, Pageable pageable);

  /** Find logs by operation type */
  Page<OperationLog> findByOperationType(String operationType, Pageable pageable);

  /** Find logs by date range */
  Page<OperationLog> findByDateRange(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  /** Search logs by multiple criteria */
  Page<OperationLog> search(
      Long userId,
      String operationType,
      String module,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Pageable pageable);

  /** Delete logs before specified date */
  void deleteBeforeDate(LocalDateTime date);

  /** Count logs by user ID */
  long countByUserId(Long userId);

  /** Count failed operations */
  long countFailedOperations(LocalDateTime startTime, LocalDateTime endTime);
}
