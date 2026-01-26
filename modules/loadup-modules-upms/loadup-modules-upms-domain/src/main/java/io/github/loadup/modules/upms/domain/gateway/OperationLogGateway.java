package io.github.loadup.modules.upms.domain.gateway;

import io.github.loadup.modules.upms.domain.entity.OperationLog;
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
public interface OperationLogGateway {

  /** Save operation log */
  OperationLog save(OperationLog log);

  /** Batch save operation logs */
  void batchSave(List<OperationLog> logs);

  /** Find log by ID */
  Optional<OperationLog> findById(String id);

  /** Find logs by user ID */
  Page<OperationLog> findByUserId(String userId, Pageable pageable);

  /** Find logs by operation type */
  Page<OperationLog> findByOperationType(String operationType, Pageable pageable);

  /** Find logs by date range */
  Page<OperationLog> findByDateRange(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  /** Search logs by multiple criteria */
  Page<OperationLog> search(
      String userId,
      String operationType,
      String module,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Pageable pageable);

  /** Delete logs before specified date */
  void deleteBeforeDate(LocalDateTime date);

  List<OperationLog> findByUserId(String userId);

  List<OperationLog> findByOperationType(String operationType);

  List<OperationLog> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

  /** Count logs by user ID */
  long countByUserId(String userId);

  Page<OperationLog> findAll(Pageable pageable);

  /** Count failed operations */
  long countFailedOperations(LocalDateTime startTime, LocalDateTime endTime);
}
