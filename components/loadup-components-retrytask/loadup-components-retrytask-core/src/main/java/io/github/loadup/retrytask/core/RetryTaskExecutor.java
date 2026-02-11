package io.github.loadup.retrytask.core;

/*-
 * #%L
 * Loadup Components Retrytask Core
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.retrytask.facade.model.RetryTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executor for retry tasks
 */
public class RetryTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RetryTaskExecutor.class);

    private final RetryTaskProcessorRegistry processorRegistry;
    private final RetryTaskService retryTaskService;
    private final Executor executor;

    public RetryTaskExecutor(
            RetryTaskProcessorRegistry processorRegistry, RetryTaskService retryTaskService, Executor executor) {
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
