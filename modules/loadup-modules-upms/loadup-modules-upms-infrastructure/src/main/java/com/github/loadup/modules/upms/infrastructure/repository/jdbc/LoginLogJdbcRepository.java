package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.infrastructure.dataobject.LoginLogDO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * LoginLog JDBC Repository
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
public interface LoginLogJdbcRepository
    extends PagingAndSortingRepository<LoginLogDO, String>, CrudRepository<LoginLogDO, String> {

  @Query("SELECT * FROM upms_login_log WHERE user_id = :userId ORDER BY login_time DESC")
  List<LoginLogDO> findByUserId(@Param("userId") String userId);

  @Query(
      """
      SELECT * FROM upms_login_log
      WHERE user_id = :userId AND login_status = 1
      ORDER BY login_time DESC
      LIMIT 1
      """)
  Optional<LoginLogDO> findLastSuccessfulLogin(@Param("userId") String userId);

  @Query(
      """
      SELECT * FROM upms_login_log
      WHERE login_time BETWEEN :startTime AND :endTime
      ORDER BY login_time DESC
      """)
  List<LoginLogDO> findByLoginTimeBetween(
      @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

  @Query(
      """
      SELECT COUNT(*) FROM upms_login_log
      WHERE user_id = :userId
      AND login_time BETWEEN :startTime AND :endTime
      """)
  long countLoginAttempts(
      @Param("userId") String userId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  @Query(
      """
      SELECT COUNT(*) FROM upms_login_log
      WHERE user_id = :userId
      AND login_status = 0
      AND login_time BETWEEN :startTime AND :endTime
      """)
  long countFailedLoginAttempts(
      @Param("userId") String userId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  @Query(
      """
      SELECT * FROM upms_login_log
      WHERE login_status = 0
      AND login_time BETWEEN :startTime AND :endTime
      ORDER BY login_time DESC
      """)
  List<LoginLogDO> findFailedLoginsBetween(
      @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

  @Modifying
  @Query("DELETE FROM upms_login_log WHERE login_time < :date")
  void deleteBeforeDate(@Param("date") LocalDateTime date);

  @Query(
      """
      SELECT * FROM upms_login_log
      WHERE login_status = 0
      AND login_time BETWEEN :startTime AND :endTime
      ORDER BY login_time DESC
      """)
  Page<LoginLogDO> findFailedLoginsBetween(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      Pageable pageable);

  @Query(
      """
      SELECT * FROM upms_login_log
      WHERE login_time BETWEEN :startTime AND :endTime
      ORDER BY login_time DESC
      """)
  Page<LoginLogDO> findByLoginTimeBetween(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      Pageable pageable);

  @Query("SELECT * FROM upms_login_log WHERE username = :username ORDER BY login_time DESC")
  Page<LoginLogDO> findByUsername(@Param("username") String username, Pageable pageable);

  @Query("SELECT * FROM upms_login_log WHERE user_id = :userId ORDER BY login_time DESC")
  Page<LoginLogDO> findByUserId(@Param("userId") String userId, Pageable pageable);
}
