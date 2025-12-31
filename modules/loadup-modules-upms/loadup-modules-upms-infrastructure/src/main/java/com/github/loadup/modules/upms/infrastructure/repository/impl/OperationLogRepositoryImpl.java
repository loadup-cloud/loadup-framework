package com.github.loadup.modules.upms.infrastructure.repository.impl;

import com.github.loadup.modules.upms.domain.entity.OperationLog;
import com.github.loadup.modules.upms.domain.repository.OperationLogRepository;
import com.github.loadup.modules.upms.infrastructure.repository.jdbc.JdbcOperationLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Operation Log Repository Implementation
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class OperationLogRepositoryImpl implements OperationLogRepository {

  private final JdbcOperationLogRepository jdbcRepository;
  private final JdbcTemplate jdbcTemplate;

  @Override
  public OperationLog save(OperationLog log) {
    return jdbcRepository.save(log);
  }

  @Override
  public void batchSave(List<OperationLog> logs) {
    jdbcRepository.saveAll(logs);
  }

  @Override
  public Optional<OperationLog> findById(Long id) {
    return jdbcRepository.findById(id);
  }

  @Override
  public Page<OperationLog> findByUserId(Long userId, Pageable pageable) {
    return jdbcRepository.findByUserId(userId, pageable);
  }

  @Override
  public Page<OperationLog> findByOperationType(String operationType, Pageable pageable) {
    return jdbcRepository.findByOperationType(operationType, pageable);
  }

  @Override
  public Page<OperationLog> findByDateRange(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
    return jdbcRepository.findByDateRange(startTime, endTime, pageable);
  }

  @Override
  public Page<OperationLog> search(
      Long userId,
      String operationType,
      String module,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Pageable pageable) {
    return jdbcRepository.search(userId, operationType, module, startTime, endTime, pageable);
  }

  @Override
  public void deleteBeforeDate(LocalDateTime date) {
    jdbcRepository.deleteBeforeDate(date);
  }

  @Override
  public long countByUserId(Long userId) {
    return jdbcRepository.countByUserId(userId);
  }

  @Override
  public long countFailedOperations(LocalDateTime startTime, LocalDateTime endTime) {
    return jdbcRepository.countFailedOperations(startTime, endTime);
  }
}
