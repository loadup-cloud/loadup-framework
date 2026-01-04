package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.domain.entity.OperationLog;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * JDBC Operation Log Repository Spring Data JDBC interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface JdbcOperationLogRepository
    extends CrudRepository<OperationLog, Long>, PagingAndSortingRepository<OperationLog, Long> {

  @Query("SELECT * FROM upms_operation_log WHERE user_id = :userId ORDER BY created_time DESC")
  Page<OperationLog> findByUserId(Long userId, Pageable pageable);

  @Query(
      "SELECT * FROM upms_operation_log WHERE operation_type = :operationType ORDER BY created_time DESC")
  Page<OperationLog> findByOperationType(String operationType, Pageable pageable);

  @Query(
      "SELECT * FROM upms_operation_log WHERE created_time BETWEEN :startTime AND :endTime ORDER BY created_time DESC")
  Page<OperationLog> findByDateRange(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  @Modifying
  @Query("DELETE FROM upms_operation_log WHERE created_time < :date")
  void deleteBeforeDate(LocalDateTime date);

  @Query("SELECT COUNT(*) FROM upms_operation_log WHERE user_id = :userId")
  long countByUserId(String userId);

  @Query(
      "SELECT * FROM upms_operation_log WHERE "
          + "(:userId IS NULL OR user_id = :userId) AND "
          + "(:operationType IS NULL OR operation_type = :operationType) AND "
          + "(:module IS NULL OR operation_module = :module) AND "
          + "(:startTime IS NULL OR created_time >= :startTime) AND "
          + "(:endTime IS NULL OR created_time <= :endTime) "
          + "ORDER BY created_time DESC")
  Page<OperationLog> search(
      Long userId,
      String operationType,
      String module,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Pageable pageable);

  @Query(
      "SELECT COUNT(*) FROM upms_operation_log WHERE status = 0 AND created_time BETWEEN :startTime AND :endTime")
  long countFailedOperations(LocalDateTime startTime, LocalDateTime endTime);
}
