package io.github.loadup.retrytask.core;

import io.github.loadup.retrytask.core.config.RetryTaskProperties;
import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.enums.Priority;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import io.github.loadup.retrytask.facade.request.RetryTaskRegisterRequest;
import io.github.loadup.retrytask.infra.repository.RetryTaskRepository;
import io.github.loadup.retrytask.notify.RetryTaskNotifier;
import io.github.loadup.retrytask.notify.RetryTaskNotifierRegistry;
import io.github.loadup.retrytask.strategy.RetryStrategy;
import io.github.loadup.retrytask.strategy.RetryStrategyRegistry;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link RetryTaskService} and {@link RetryTaskFacade}
 */
public class RetryTaskServiceImpl implements RetryTaskService, RetryTaskFacade {
    private final RetryTaskRepository retryTaskRepository;
    private final RetryTaskNotifierRegistry retryTaskNotifierRegistry;
    private final RetryStrategyRegistry retryStrategyRegistry;
    private final RetryTaskProperties retryTaskProperties;
    private final RetryTaskExecutor retryTaskExecutor;

    public RetryTaskServiceImpl(RetryTaskRepository retryTaskRepository,
                                RetryTaskNotifierRegistry retryTaskNotifierRegistry,
                                RetryStrategyRegistry retryStrategyRegistry,
                                RetryTaskProperties retryTaskProperties,
                                @Lazy RetryTaskExecutor retryTaskExecutor) {
        this.retryTaskRepository = retryTaskRepository;
        this.retryTaskNotifierRegistry = retryTaskNotifierRegistry;
        this.retryStrategyRegistry = retryStrategyRegistry;
        this.retryTaskProperties = retryTaskProperties;
        this.retryTaskExecutor = retryTaskExecutor;
    }

    @Override
    @Transactional
    public Long register(RetryTaskRegisterRequest request) {
        RetryTask task = new RetryTask();
        task.setBizType(request.getBizType());
        task.setBizId(request.getBizId());
        task.setRetryCount(0);

        RetryTaskProperties.BizTypeConfig config = Optional.ofNullable(retryTaskProperties.getBizTypes())
                .map(bizTypes -> bizTypes.get(request.getBizType()))
                .orElse(new RetryTaskProperties.BizTypeConfig());

        // Determine priority: Request > Config > Default
        Priority priority = request.getPriority();
        if (priority == null) {
            priority = "H".equals(config.getPriority()) ? Priority.HIGH : Priority.LOW;
        }
        task.setPriority(priority);

        task.setMaxRetryCount(config.getMaxRetryCount());
        task.setNextRetryTime(LocalDateTime.now());
        task.setStatus(RetryTaskStatus.PENDING);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        RetryTask savedTask = retryTaskRepository.save(task);

        // Immediate Execution Logic
        boolean executeImmediately = request.getExecuteImmediately() != null ?
                request.getExecuteImmediately() : config.isExecuteImmediately();

        if (executeImmediately) {
            boolean waitResult = request.getWaitResult() != null ?
                    request.getWaitResult() : config.isWaitResult();

            if (waitResult) {
                retryTaskExecutor.executeSync(savedTask);
            } else {
                retryTaskExecutor.executeAsync(savedTask);
            }
        }

        return savedTask.getId();
    }

    @Override
    @Transactional
    public void delete(String bizType, String bizId) {
        retryTaskRepository.delete(bizType, bizId);
    }

    @Override
    @Transactional
    public void reset(String bizType, String bizId) {
        retryTaskRepository.findByBizTypeAndBizId(bizType, bizId).ifPresent(task -> {
            task.setRetryCount(0);
            task.setNextRetryTime(LocalDateTime.now());
            task.setStatus(RetryTaskStatus.PENDING);
            task.setUpdateTime(LocalDateTime.now());
            retryTaskRepository.save(task);
        });
    }

    @Override
    public List<RetryTask> pullTasks(int batchSize) {
        return retryTaskRepository.findTasksToRetry(LocalDateTime.now(), batchSize);
    }

    @Override
    public List<RetryTask> pullTasks(String bizType, int batchSize) {
        return retryTaskRepository.findTasksToRetryByBizType(bizType, LocalDateTime.now(), batchSize);
    }

    @Override
    @Transactional
    public void markSuccess(Long taskId) {
        retryTaskRepository.deleteById(taskId);
    }

    @Override
    @Transactional
    public void markFailure(Long taskId, String reason) {
        retryTaskRepository.findById(taskId).ifPresent(task -> {
            task.setRetryCount(task.getRetryCount() + 1);
            task.setLastFailureReason(reason);

            RetryTaskProperties.BizTypeConfig config = Optional.ofNullable(retryTaskProperties.getBizTypes())
                    .map(bizTypes -> bizTypes.get(task.getBizType()))
                    .orElse(new RetryTaskProperties.BizTypeConfig());

            if (task.getRetryCount() >= config.getMaxRetryCount()) {
                task.setStatus(RetryTaskStatus.FAILURE);
                RetryTaskNotifier notifier = retryTaskNotifierRegistry.getNotifier(config.getNotifier());
                if (notifier != null) {
                    notifier.notify(task);
                } else {
                    // Fallback to default log notifier
                    retryTaskNotifierRegistry.getNotifier("log").notify(task);
                }
            } else {
                RetryStrategy strategy = retryStrategyRegistry.getStrategy(config.getStrategy());
                if (strategy == null) {
                    strategy = retryStrategyRegistry.getStrategy("fixed"); // Fallback to default
                }
                task.setNextRetryTime(strategy.nextRetryTime(task));
            }
            task.setUpdateTime(LocalDateTime.now());
            retryTaskRepository.save(task);
        });
    }

    @Override
    @Transactional
    public boolean tryLock(Long taskId) {
        return retryTaskRepository.tryLock(taskId);
    }

    @Override
    @Transactional
    public int resetStuckTasks(LocalDateTime deadTime) {
        return retryTaskRepository.resetStuckTasks(deadTime);
    }
}
