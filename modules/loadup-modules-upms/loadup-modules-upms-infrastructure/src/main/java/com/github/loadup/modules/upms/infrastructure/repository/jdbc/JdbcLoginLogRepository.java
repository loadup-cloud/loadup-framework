package com.github.loadup.modules.upms.infrastructure.repository.jdbc;

import com.github.loadup.modules.upms.domain.entity.LoginLog;
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

/**
 * Spring Data JDBC LoginLog Repository
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface JdbcLoginLogRepository
    extends CrudRepository<LoginLog, Long>, PagingAndSortingRepository<LoginLog, Long> {

  @Query("SELECT * FROM upms_login_log WHERE user_id = :userId ORDER BY login_time DESC")
  Page<LoginLog> findByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query("SELECT * FROM upms_login_log WHERE username = :username ORDER BY login_time DESC")
  Page<LoginLog> findByUsername(@Param("username") String username, Pageable pageable);

  @Query(
      """
        SELECT * FROM upms_login_log
        WHERE login_time >= :startTime AND login_time <= :endTime
        ORDER BY login_time DESC
    """)
  Page<LoginLog> findByDateRange(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      Pageable pageable);

  @Query(
      """
        SELECT * FROM upms_login_log
        WHERE login_status = 0
        AND login_time >= :startTime AND login_time <= :endTime
        ORDER BY login_time DESC
    """)
  Page<LoginLog> findFailedLogins(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      Pageable pageable);

  @Query(
      """
        SELECT COUNT(*) FROM upms_login_log
        WHERE user_id = :userId
        AND login_time >= :startTime AND login_time <= :endTime
    """)
  long countLoginAttempts(
      @Param("userId") Long userId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  @Query(
      """
        SELECT COUNT(*) FROM upms_login_log
        WHERE user_id = :userId
        AND login_status = 0
        AND login_time >= :startTime AND login_time <= :endTime
    """)
  long countFailedLoginAttempts(
      @Param("userId") Long userId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  @Query(
      """
        SELECT * FROM upms_login_log
        WHERE user_id = :userId AND login_status = 1
        ORDER BY login_time DESC
        LIMIT 1
    """)
  Optional<LoginLog> findLastSuccessfulLogin(@Param("userId") Long userId);

  @Modifying
  @Query("DELETE FROM upms_login_log WHERE login_time < :date")
  void deleteBeforeDate(@Param("date") LocalDateTime date);
}
