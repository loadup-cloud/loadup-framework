package io.github.loadup.retrytask.core;

import io.github.loadup.retrytask.facade.model.RetryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 * Executor for retry tasks
 */
public class RetryTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RetryTaskExecutor.class);

    private final RetryTaskProcessorRegistry processorRegistry;
    private final RetryTaskService retryTaskService;
    private final Executor executor;

    public RetryTaskExecutor(RetryTaskProcessorRegistry processorRegistry,
                           RetryTaskService retryTaskService,
                           Executor executor) {
        this.processorRegistry = processorRegistry;
        this.retryTaskService = retryTaskService;
        this.executor = executor != null ? executor : ForkJoinPool.commonPool();
    }

    /**
     * Executes a retry task asynchronously.
     * Use this for scheduler or fire-and-forget.
     *
     * @param task The task to execute.
     */
    public void executeAsync(RetryTask task) {
        executor.execute(() -> executeSync(task));
    }

    /**
     * Executes a retry task synchronously.
     * Use this for immediate execution where result is needed.
     *
     * @param task The task to execute.
     */
    public void executeSync(RetryTask task) {
        // Optimistic locking: PENDING -> RUNNING
        if (!retryTaskService.tryLock(task.getId())) {
            logger.debug("Task {} locked by other node or not in PENDING status", task.getId());
            return;
        }

        RetryTaskProcessor processor = processorRegistry.getProcessor(task.getBizType());
        if (processor == null) {
            logger.warn("No processor found for bizType: {}", task.getBizType());
            // Mark failure if no processor found to avoid stuck in RUNNING
            retryTaskService.markFailure(task.getId(), "No processor found");
            return;
        }

        try {
            boolean success = processor.process(task);
            if (success) {
                retryTaskService.markSuccess(task.getId());
            } else {
                retryTaskService.markFailure(task.getId(), "Processor returned false");
            }
        } catch (Exception e) {
            logger.error("Error processing task: {}", task.getId(), e);
            retryTaskService.markFailure(task.getId(), e.getMessage());
        }
    }
}
