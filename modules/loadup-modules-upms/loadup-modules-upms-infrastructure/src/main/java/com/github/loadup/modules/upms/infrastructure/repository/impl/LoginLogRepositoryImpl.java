package com.github.loadup.modules.upms.infrastructure.repository.impl;

import com.github.loadup.modules.upms.domain.entity.LoginLog;
import com.github.loadup.modules.upms.domain.repository.LoginLogRepository;
import com.github.loadup.modules.upms.infrastructure.dataobject.LoginLogDO;
import com.github.loadup.modules.upms.infrastructure.mapper.LoginLogMapper;
import com.github.loadup.modules.upms.infrastructure.repository.jdbc.LoginLogJdbcRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

  private final LoginLogJdbcRepository jdbcRepository;
  private final LoginLogMapper loginLogMapper;

  @Override
  public LoginLog save(LoginLog loginLog) {
    LoginLogDO loginLogDO = loginLogMapper.toDO(loginLog);
    LoginLogDO saved = jdbcRepository.save(loginLogDO);
    return loginLogMapper.toEntity(saved);
  }

  @Override
  public Optional<LoginLog> findById(String id) {
    return jdbcRepository.findById(id).map(loginLogMapper::toEntity);
  }

  @Override
  public List<LoginLog> findByUserId(String userId) {
    List<LoginLogDO> loginLogDOList = jdbcRepository.findByUserId(userId);
    return loginLogMapper.toEntityList(loginLogDOList);
  }

  @Override
  public Optional<LoginLog> findLastSuccessfulLogin(String userId) {
    return jdbcRepository.findLastSuccessfulLogin(userId).map(loginLogMapper::toEntity);
  }

  @Override
  public List<LoginLog> findByLoginTimeBetween(LocalDateTime startTime, LocalDateTime endTime) {
    List<LoginLogDO> loginLogDOList = jdbcRepository.findByLoginTimeBetween(startTime, endTime);
    return loginLogMapper.toEntityList(loginLogDOList);
  }

  @Override
  public Page<LoginLog> findAll(Pageable pageable) {
    Page<LoginLogDO> loginLogDOPage = jdbcRepository.findAll(pageable);
    List<LoginLog> loginLogs = loginLogMapper.toEntityList(loginLogDOPage.getContent());
    return new PageImpl<>(loginLogs, pageable, loginLogDOPage.getTotalElements());
  }

  @Override
  public long countLoginAttempts(String userId, LocalDateTime startTime, LocalDateTime endTime) {
    return jdbcRepository.countLoginAttempts(userId, startTime, endTime);
  }

  @Override
  public long countFailedLoginAttempts(
      String userId, LocalDateTime startTime, LocalDateTime endTime) {
    return jdbcRepository.countFailedLoginAttempts(userId, startTime, endTime);
  }

  @Override
  public void deleteBeforeDate(LocalDateTime date) {
    jdbcRepository.deleteBeforeDate(date);
  }

  @Override
  public Page<LoginLog> findFailedLogins(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
    Page<LoginLogDO> loginLogDOPage =
        jdbcRepository.findFailedLoginsBetween(startTime, endTime, pageable);
    List<LoginLog> loginLogs = loginLogMapper.toEntityList(loginLogDOPage.getContent());
    return new PageImpl<>(loginLogs, pageable, loginLogDOPage.getTotalElements());
  }

  @Override
  public Page<LoginLog> findByDateRange(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
    Page<LoginLogDO> loginLogDOPage =
        jdbcRepository.findByLoginTimeBetween(startTime, endTime, pageable);
    List<LoginLog> loginLogs = loginLogMapper.toEntityList(loginLogDOPage.getContent());
    return new PageImpl<>(loginLogs, pageable, loginLogDOPage.getTotalElements());
  }

  @Override
  public Page<LoginLog> findByUsername(String username, Pageable pageable) {
    Page<LoginLogDO> loginLogDOPage = jdbcRepository.findByUsername(username, pageable);
    List<LoginLog> loginLogs = loginLogMapper.toEntityList(loginLogDOPage.getContent());
    return new PageImpl<>(loginLogs, pageable, loginLogDOPage.getTotalElements());
  }

  @Override
  public Page<LoginLog> findByUserId(String userId, Pageable pageable) {
    Page<LoginLogDO> loginLogDOPage = jdbcRepository.findByUserId(userId, pageable);
    List<LoginLog> loginLogs = loginLogMapper.toEntityList(loginLogDOPage.getContent());
    return new PageImpl<>(loginLogs, pageable, loginLogDOPage.getTotalElements());
  }
}
