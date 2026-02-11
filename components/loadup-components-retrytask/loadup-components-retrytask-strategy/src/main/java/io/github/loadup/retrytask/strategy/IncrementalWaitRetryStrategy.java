package io.github.loadup.retrytask.strategy;

import io.github.loadup.retrytask.facade.model.RetryTask;

import java.time.LocalDateTime;

/**
 * Incremental wait retry strategy - wait time increases linearly
 * Formula: delay = baseDelay * (retryCount + 1)
 */
public class IncrementalWaitRetryStrategy implements RetryStrategy {

    public static final String TYPE = "incremental";

    private static final int DEFAULT_BASE_DELAY_SECONDS = 30;

    @Override
    public LocalDateTime nextRetryTime(RetryTask task) {
        int baseDelay = DEFAULT_BASE_DELAY_SECONDS;

        // Linear increase: 30s, 60s, 90s, 120s, ...
        long delaySeconds = baseDelay * (task.getRetryCount() + 1);

        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}

