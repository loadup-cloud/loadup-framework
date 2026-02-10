package io.github.loadup.retrytask.strategy;

import io.github.loadup.retrytask.facade.model.RetryTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * A retry strategy that retries after a fixed delay.
 */
@Component
public class FixedRetryStrategy implements RetryStrategy {

    public static final String TYPE = "fixed";

    @Override
    public LocalDateTime nextRetryTime(RetryTask task) {
        return LocalDateTime.now().plusMinutes(1); // Default value, should be configurable
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
