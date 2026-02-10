package io.github.loadup.retrytask.strategy;

import io.github.loadup.retrytask.facade.model.RetryTask;

import java.time.LocalDateTime;

/**
 * A strategy for calculating the next retry time.
 */
public interface RetryStrategy {

    /**
     * Calculates the next retry time.
     *
     * @param task The task to calculate the next retry time for.
     * @return The next retry time.
     */
    LocalDateTime nextRetryTime(RetryTask task);

    /**
     * Gets the type of the strategy.
     *
     * @return The type of the strategy.
     */
    String getType();
}
