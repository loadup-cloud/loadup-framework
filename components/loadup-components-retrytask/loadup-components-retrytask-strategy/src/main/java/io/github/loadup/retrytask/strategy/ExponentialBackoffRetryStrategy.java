package io.github.loadup.retrytask.strategy;

import io.github.loadup.retrytask.facade.model.RetryTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * A retry strategy that retries with an exponential backoff.
 */
@Component
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
