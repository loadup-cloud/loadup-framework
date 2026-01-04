package com.github.loadup.modules.upms.infrastructure.repository.impl;

import com.github.loadup.modules.upms.domain.entity.OperationLog;
import com.github.loadup.modules.upms.domain.repository.OperationLogRepository;
import com.github.loadup.modules.upms.infrastructure.dataobject.OperationLogDO;
import com.github.loadup.modules.upms.infrastructure.mapper.OperationLogMapper;
import com.github.loadup.modules.upms.infrastructure.repository.jdbc.OperationLogJdbcRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * OperationLog Repository Implementation
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class OperationLogRepositoryImpl implements OperationLogRepository {

  private final OperationLogJdbcRepository jdbcRepository;
  private final OperationLogMapper operationLogMapper;

  @Override
  public OperationLog save(OperationLog operationLog) {
    OperationLogDO operationLogDO = operationLogMapper.toDO(operationLog);
    OperationLogDO saved = jdbcRepository.save(operationLogDO);
    return operationLogMapper.toEntity(saved);
  }

  @Override
  public void batchSave(List<OperationLog> operationLogs) {
    List<OperationLogDO> operationLogDOs = operationLogMapper.toDOList(operationLogs);
    operationLogDOs.forEach(jdbcRepository::save);
  }

  @Override
  public Optional<OperationLog> findById(String id) {
    return jdbcRepository.findById(id).map(operationLogMapper::toEntity);
  }

  @Override
  public List<OperationLog> findByUserId(String userId) {
    List<OperationLogDO> operationLogDOList = jdbcRepository.findByUserId(userId);
    return operationLogMapper.toEntityList(operationLogDOList);
  }

  @Override
  public List<OperationLog> findByOperationType(String operationType) {
    List<OperationLogDO> operationLogDOList = jdbcRepository.findByOperationType(operationType);
    return operationLogMapper.toEntityList(operationLogDOList);
  }

  @Override
  public List<OperationLog> findByCreatedTimeBetween(
      LocalDateTime startTime, LocalDateTime endTime) {
    List<OperationLogDO> operationLogDOList =
        jdbcRepository.findByCreatedTimeBetween(startTime, endTime);
    return operationLogMapper.toEntityList(operationLogDOList);
  }

  @Override
  public long countByUserId(String userId) {
    return jdbcRepository.countByUserId(userId);
  }

  @Override
  public Page<OperationLog> findAll(Pageable pageable) {
    // TODO: implement pagination
    return new PageImpl<>(List.of(), pageable, 0);
  }

  @Override
  public long countFailedOperations(LocalDateTime startTime, LocalDateTime endTime) {
    return jdbcRepository.countFailedOperations(startTime, endTime);
  }

  @Override
  public void deleteBeforeDate(LocalDateTime date) {
    // TODO: implement delete with JdbcTemplate
    // DELETE FROM upms_operation_log WHERE created_time < :date
  }

  @Override
  public Page<OperationLog> search(
      String userId,
      String operationType,
      String operationModule,
      LocalDateTime startTime,
      LocalDateTime endTime,
      Pageable pageable) {
    // TODO: implement search with dynamic query builder
    // For now return empty page
    return new PageImpl<>(List.of(), pageable, 0);
  }

  @Override
  public Page<OperationLog> findByDateRange(
      LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
    List<OperationLogDO> operationLogDOList =
        jdbcRepository.findByCreatedTimeBetween(startTime, endTime);
    List<OperationLog> operationLogs = operationLogMapper.toEntityList(operationLogDOList);
    // TODO: implement real pagination
    return new PageImpl<>(operationLogs, pageable, operationLogs.size());
  }

  @Override
  public Page<OperationLog> findByOperationType(String operationType, Pageable pageable) {
    List<OperationLogDO> operationLogDOList = jdbcRepository.findByOperationType(operationType);
    List<OperationLog> operationLogs = operationLogMapper.toEntityList(operationLogDOList);
    // TODO: implement real pagination
    return new PageImpl<>(operationLogs, pageable, operationLogs.size());
  }

  @Override
  public Page<OperationLog> findByUserId(String userId, Pageable pageable) {
    List<OperationLogDO> operationLogDOList = jdbcRepository.findByUserId(userId);
    List<OperationLog> operationLogs = operationLogMapper.toEntityList(operationLogDOList);
    // TODO: implement real pagination
    return new PageImpl<>(operationLogs, pageable, operationLogs.size());
  }
}
