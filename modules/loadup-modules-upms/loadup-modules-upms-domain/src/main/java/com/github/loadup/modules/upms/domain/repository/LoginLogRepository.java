package com.github.loadup.modules.upms.domain.repository;

import com.github.loadup.modules.upms.domain.entity.LoginLog;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Login Log Repository Interface
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public interface LoginLogRepository {

  /** Save login log */
  LoginLog save(LoginLog log);

  /** Find log by ID */
  Optional<LoginLog> findById(Long id);

  /** Find logs by user ID */
  Page<LoginLog> findByUserId(Long userId, Pageable pageable);

  /** Find logs by username */
  Page<LoginLog> findByUsername(String username, Pageable pageable);

  /** Find logs by date range */
  Page<LoginLog> findByDateRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  /** Find failed login attempts */
  Page<LoginLog> findFailedLogins(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  /** Delete logs before specified date */
  void deleteBeforeDate(LocalDateTime date);

  /** Count login attempts by user and time range */
  long countLoginAttempts(Long userId, LocalDateTime startTime, LocalDateTime endTime);

  /** Count failed login attempts by user and time range */
  long countFailedLoginAttempts(Long userId, LocalDateTime startTime, LocalDateTime endTime);

  /** Get last successful login */
  Optional<LoginLog> findLastSuccessfulLogin(Long userId);
}
