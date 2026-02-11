package io.github.loadup.retrytask.strategy;

import io.github.loadup.retrytask.facade.model.RetryTask;

import java.time.LocalDateTime;

/**
 * Fixed delay retry strategy - waits a fixed time between retries
 * Default: 60 seconds
 */
public class FixedRetryStrategy implements RetryStrategy {

    public static final String TYPE = "fixed";

    private static final int DEFAULT_DELAY_SECONDS = 60;

    @Override
    public LocalDateTime nextRetryTime(RetryTask task) {
        return LocalDateTime.now().plusSeconds(DEFAULT_DELAY_SECONDS);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
