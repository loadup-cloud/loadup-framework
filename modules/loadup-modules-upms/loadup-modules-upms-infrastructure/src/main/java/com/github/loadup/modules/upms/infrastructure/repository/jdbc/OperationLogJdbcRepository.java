package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.infrastructure.dataobject.OperationLogDO;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * OperationLog JDBC Repository
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
public interface OperationLogJdbcRepository
    extends PagingAndSortingRepository<OperationLogDO, String>,
        CrudRepository<OperationLogDO, String> {

  @Query("SELECT * FROM upms_operation_log WHERE user_id = :userId ORDER BY created_time DESC")
  List<OperationLogDO> findByUserId(@Param("userId") String userId);

  @Query(
      """
      SELECT * FROM upms_operation_log
      WHERE operation_type = :operationType
      ORDER BY created_time DESC
      """)
  List<OperationLogDO> findByOperationType(@Param("operationType") String operationType);

  @Query(
      """
      SELECT * FROM upms_operation_log
      WHERE created_time BETWEEN :startTime AND :endTime
      ORDER BY created_time DESC
      """)
  List<OperationLogDO> findByCreatedTimeBetween(
      @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

  @Query("SELECT COUNT(*) FROM upms_operation_log WHERE user_id = :userId")
  long countByUserId(@Param("userId") String userId);

  @Query(
      """
      SELECT COUNT(*) FROM upms_operation_log
      WHERE status = 0
      AND created_time BETWEEN :startTime AND :endTime
      """)
  long countFailedOperations(
      @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

  @Modifying
  @Query("DELETE FROM upms_operation_log WHERE created_time < :date")
  void deleteBeforeDate(@Param("date") LocalDateTime date);

  @Query(
      "SELECT * FROM upms_operation_log WHERE operation_type = :operationType ORDER BY created_time DESC")
  Page<OperationLogDO> findByOperationType(
      @Param("operationType") String operationType, Pageable pageable);

  @Query("SELECT * FROM upms_operation_log WHERE user_id = :userId ORDER BY created_time DESC")
  Page<OperationLogDO> findByUserId(@Param("userId") String userId, Pageable pageable);

  @Query(
      """
      SELECT * FROM upms_operation_log
      WHERE created_time BETWEEN :startTime AND :endTime
      ORDER BY created_time DESC
      """)
  Page<OperationLogDO> findByCreatedTimeBetween(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      Pageable pageable);
}
