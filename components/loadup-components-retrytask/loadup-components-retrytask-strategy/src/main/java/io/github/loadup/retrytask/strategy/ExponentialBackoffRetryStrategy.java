package io.github.loadup.retrytask.strategy;

import io.github.loadup.retrytask.facade.model.RetryTask;

import java.time.LocalDateTime;

/**
 * Exponential backoff retry strategy - wait time increases exponentially
 * Formula: delay = 2^retryCount minutes
 * Example: 2min, 4min, 8min, 16min, 32min, ...
 */
public class ExponentialBackoffRetryStrategy implements RetryStrategy {

    public static final String TYPE = "exponential";

    @Override
    public LocalDateTime nextRetryTime(RetryTask task) {
        long delay = (long) Math.pow(2, task.getRetryCount());
        return LocalDateTime.now().plusMinutes(delay);
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
