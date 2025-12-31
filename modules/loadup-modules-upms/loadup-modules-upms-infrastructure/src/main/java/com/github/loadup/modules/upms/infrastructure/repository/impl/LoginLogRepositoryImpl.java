package com.github.loadup.modules.upms.infrastructure.repository.impl;

import com.github.loadup.modules.upms.domain.entity.LoginLog;
import com.github.loadup.modules.upms.domain.repository.LoginLogRepository;
import com.github.loadup.modules.upms.infrastructure.repository.jdbc.JdbcLoginLogRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * LoginLog Repository Implementation
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class LoginLogRepositoryImpl implements LoginLogRepository {

  private final JdbcLoginLogRepository jdbcRepository;

  @Override
  public LoginLog save(LoginLog log) {
    return jdbcRepository.save(log);
  }

  @Override
  public Optional<LoginLog> findById(Long id) {
    return jdbcRepository.findById(id);
  }

  @Override
  public Page<LoginLog> findByUserId(Long userId, Pageable pageable) {
    return jdbcRepository.findByUserId(userId, pageable);
  }

  @Override
  public Page<LoginLog> findByUsername(String username, Pageable pageable) {
    return jdbcRepository.findByUsername(username, pageable);
  }

  @Override
  public Page<LoginLog> findByDateRange(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
    return jdbcRepository.findByDateRange(startTime, endTime, pageable);
  }

  @Override
  public Page<LoginLog> findFailedLogins(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
    return jdbcRepository.findFailedLogins(startTime, endTime, pageable);
  }

  @Override
  public void deleteBeforeDate(LocalDateTime date) {
    jdbcRepository.deleteBeforeDate(date);
  }

  @Override
  public long countLoginAttempts(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
    return jdbcRepository.countLoginAttempts(userId, startTime, endTime);
  }

  @Override
  public long countFailedLoginAttempts(
      Long userId, LocalDateTime startTime, LocalDateTime endTime) {
    return jdbcRepository.countFailedLoginAttempts(userId, startTime, endTime);
  }

  @Override
  public Optional<LoginLog> findLastSuccessfulLogin(Long userId) {
    return jdbcRepository.findLastSuccessfulLogin(userId);
  }
}
