package io.github.loadup.retrytask.infra.mysql.repository;

import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.model.RetryTaskStatus;
import io.github.loadup.retrytask.infra.api.management.RetryTaskManagement;
import io.github.loadup.retrytask.infra.mysql.mapper.RetryTaskDOMapper;
import io.github.loadup.retrytask.infra.mysql.model.RetryTaskDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The default implementation of the {@link RetryTaskManagement}.
 */
@Component
public class RetryTaskManagementImpl implements RetryTaskManagement {

    private final RetryTaskDORepository dboRepository;

    @Autowired
    public RetryTaskManagementImpl(RetryTaskDORepository dboRepository) {
        this.dboRepository = dboRepository;
    }

    @Override
    public RetryTask save(RetryTask task) {
        RetryTaskDO dbo = RetryTaskDOMapper.toDbo(task);
        RetryTaskDO savedDbo = dboRepository.save(dbo);
        return RetryTaskDOMapper.toDomain(savedDbo);
    }

    @Override
    public Optional<RetryTask> findByBizTypeAndBizId(String bizType, String bizId) {
        return dboRepository.findByBizTypeAndBizId(bizType, bizId)
                .map(RetryTaskDOMapper::toDomain);
    }

    @Override
    public void delete(String bizType, String bizId) {
        dboRepository.delete(bizType, bizId);
    }

    @Override
    public List<RetryTask> findTasksToRetry(LocalDateTime nextRetryTime, int limit) {
        return dboRepository.findTasksToRetry(
                        nextRetryTime, RetryTaskStatus.PENDING, PageRequest.of(0, limit))
                .stream()
                .map(RetryTaskDOMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RetryTask> findById(Long id) {
        return dboRepository.findById(id)
                .map(RetryTaskDOMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        dboRepository.deleteById(id);
    }
}
