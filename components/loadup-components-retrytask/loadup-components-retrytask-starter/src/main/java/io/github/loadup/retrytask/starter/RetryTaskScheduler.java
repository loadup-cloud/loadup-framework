package io.github.loadup.retrytask.starter;

import io.github.loadup.components.scheduler.annotation.DistributedScheduler;
import io.github.loadup.retrytask.core.RetryTaskExecutor;
import io.github.loadup.retrytask.core.RetryTaskService;
import io.github.loadup.retrytask.core.config.RetryTaskProperties;
import io.github.loadup.retrytask.facade.model.RetryTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Scheduler for scanning and executing retry tasks
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "loadup.retrytask", name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class RetryTaskScheduler {

    private final RetryTaskService retryTaskService;
    private final RetryTaskExecutor retryTaskExecutor;
    private final RetryTaskProperties retryTaskProperties;

    public RetryTaskScheduler(RetryTaskService retryTaskService, RetryTaskExecutor retryTaskExecutor, RetryTaskProperties retryTaskProperties) {
        this.retryTaskService = retryTaskService;
        this.retryTaskExecutor = retryTaskExecutor;
        this.retryTaskProperties = retryTaskProperties;
        log.info(">>> [RETRY-TASK] RetryTaskScheduler initialized");
    }

    /**
     * Scan and execute pending retry tasks
     * Default: every 1 minute
     */
    @DistributedScheduler(name = "RetryTaskScan", cron = "${loadup.retrytask.scheduler.scan-cron:0 * * * * ?}", bizTag = "retry")
    public void scanAndExecute() {
        try {
            int batchSize = 100; // TODO: make configurable

            Set<String> bizTypes = retryTaskProperties.getBizTypes() != null ? retryTaskProperties.getBizTypes().keySet() : Collections.emptySet();

            if (bizTypes.isEmpty()) {
                // If no bizTypes configured, fallback to global pull
                scanAndExecuteInternal(null, batchSize);
            } else {
                // Pull for each bizType to avoid starvation
                for (String bizType : bizTypes) {
                    scanAndExecuteInternal(bizType, batchSize);
                }

                // TODO: Consider how to handle tasks with bizTypes NOT in config.
                // Currently they might be missed if we strictly follow this loop.
                // A hybrid approach (specific pulls + global pull) might be needed but global pull could re-introduce starvation.
                // For now, assuming users configure their bizTypes for heavy loads.
            }

        } catch (Exception e) {
            log.error(">>> [RETRY-TASK] Error in scheduler", e);
        }
    }

    private void scanAndExecuteInternal(String bizType, int batchSize) {
        // Pull pending tasks
        List<RetryTask> tasks;
        if (bizType != null) {
            tasks = retryTaskService.pullTasks(bizType, batchSize);
        } else {
            tasks = retryTaskService.pullTasks(batchSize);
        }

        if (tasks.isEmpty()) {
            return;
        }

        log.info(">>> [RETRY-TASK] Found {} pending tasks to execute for bizType={}", tasks.size(), bizType != null ? bizType : "ALL");

        // Sort by priority (H > L) and next retry time
        tasks.sort(Comparator.comparing(RetryTask::getPriority).reversed().thenComparing(RetryTask::getNextRetryTime));

        // Execute tasks
        for (RetryTask task : tasks) {
            try {
                retryTaskExecutor.executeAsync(task);
            } catch (Exception e) {
                log.error(">>> [RETRY-TASK] Error submitting task: bizType={}, bizId={}", task.getBizType(), task.getBizId(), e);
            }
        }
    }

    /**
     * Scan and reset stuck tasks (tasks stuck in RUNNING state for too long)
     * Default: every 5 minutes
     */
    @DistributedScheduler(name = "RetryTaskZombieCheck", cron = "${loadup.retrytask.scheduler.zombie-check-cron:0 */5 * * * * ?}", bizTag = "retry")
    public void resetStuckTasks() {
        try {
            // Tasks running for more than 5 minutes are considered stuck
            // TODO: make threshold configurable
            LocalDateTime deadTime = LocalDateTime.now().minusMinutes(5);

            int count = retryTaskService.resetStuckTasks(deadTime);

            if (count > 0) {
                log.warn(">>> [RETRY-TASK] Reset {} stuck tasks", count);
            }
        } catch (Exception e) {
            log.error(">>> [RETRY-TASK] Error resetting stuck tasks", e);
        }
    }
}
