package io.github.loadup.retrytask.strategy;

/*-
 * #%L
 * Loadup Components Retrytask Strategy
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
