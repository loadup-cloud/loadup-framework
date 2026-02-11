package io.github.loadup.retrytask.strategy;

import io.github.loadup.retrytask.facade.model.RetryTask;

import java.time.LocalDateTime;

/**
 * Fibonacci backoff retry strategy - wait time follows Fibonacci sequence
 * Formula: delay = fibonacci(retryCount) * baseDelay
 * Example: 1s, 1s, 2s, 3s, 5s, 8s, 13s, 21s, 34s, 55s, ...
 */
public class FibonacciBackoffRetryStrategy implements RetryStrategy {

    public static final String TYPE = "fibonacci";

    private static final int DEFAULT_BASE_DELAY_SECONDS = 1;

    @Override
    public LocalDateTime nextRetryTime(RetryTask task) {
        int baseDelay = DEFAULT_BASE_DELAY_SECONDS;

        // Calculate Fibonacci number for retry count
        long fibNumber = fibonacci(task.getRetryCount());
        long delaySeconds = fibNumber * baseDelay;

        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Calculate Fibonacci number
     * @param n the position in Fibonacci sequence
     * @return the Fibonacci number
     */
    private long fibonacci(int n) {
        if (n <= 1) {
            return 1;
        }

        long a = 1, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }
}

