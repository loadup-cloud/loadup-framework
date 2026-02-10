package io.github.loadup.retrytask.core;

import io.github.loadup.retrytask.facade.model.RetryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The executor for retry tasks.
 */
@Component
public class RetryTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RetryTaskExecutor.class);

    private final RetryTaskProcessorRegistry processorRegistry;
    private final RetryTaskService retryTaskService;

    @Autowired
    public RetryTaskExecutor(RetryTaskProcessorRegistry processorRegistry, RetryTaskService retryTaskService) {
        this.processorRegistry = processorRegistry;
        this.retryTaskService = retryTaskService;
    }

    /**
     * Executes a retry task.
     *
     * @param task The task to execute.
     */
    public void execute(RetryTask task) {
        RetryTaskProcessor processor = processorRegistry.getProcessor(task.getBizType());
        if (processor == null) {
            logger.warn("No processor found for bizType: {}", task.getBizType());
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
