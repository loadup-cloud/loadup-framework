package io.github.loadup.retrytask.test.strategy;

import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.strategy.ExponentialBackoffRetryStrategy;
import io.github.loadup.retrytask.strategy.FixedRetryStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for retry strategies
 */
class RetryStrategyTest {

    private RetryTask retryTask;

    @BeforeEach
    void setUp() {
        retryTask = new RetryTask();
        retryTask.setRetryCount(0);
    }

    @Test
    @DisplayName("FixedRetryStrategy should return a time in the future")
    void testFixedRetryStrategy() {
        // Given
        FixedRetryStrategy strategy = new FixedRetryStrategy();
        LocalDateTime now = LocalDateTime.now();

        // When
        LocalDateTime nextRetryTime = strategy.nextRetryTime(retryTask);

        // Then
        assertNotNull(nextRetryTime);
        assertTrue(nextRetryTime.isAfter(now) || nextRetryTime.isEqual(now));
    }

    @Test
    @DisplayName("ExponentialBackoffRetryStrategy should increase delay with each retry")
    void testExponentialBackoffRetryStrategy() {
        // Given
        ExponentialBackoffRetryStrategy strategy = new ExponentialBackoffRetryStrategy();
        LocalDateTime now = LocalDateTime.now();

        // When - First retry
        retryTask.setRetryCount(0);
        LocalDateTime firstRetryTime = strategy.nextRetryTime(retryTask);

        // Then
        assertTrue(firstRetryTime.isAfter(now.plusSeconds(0)) && firstRetryTime.isBefore(now.plusMinutes(2)));

        // When - Second retry
        retryTask.setRetryCount(1);
        LocalDateTime secondRetryTime = strategy.nextRetryTime(retryTask);

        // Then
        assertTrue(secondRetryTime.isAfter(firstRetryTime));

        // When - Third retry
        retryTask.setRetryCount(2);
        LocalDateTime thirdRetryTime = strategy.nextRetryTime(retryTask);

        // Then
        assertTrue(thirdRetryTime.isAfter(secondRetryTime));
    }

    @Test
    @DisplayName("Strategies should handle high retry counts")
    void testHighRetryCounts() {
        // Given
        FixedRetryStrategy fixedStrategy = new FixedRetryStrategy();
        ExponentialBackoffRetryStrategy exponentialStrategy = new ExponentialBackoffRetryStrategy();
        retryTask.setRetryCount(10);

        // When & Then
        assertDoesNotThrow(() -> fixedStrategy.nextRetryTime(retryTask));
        assertDoesNotThrow(() -> exponentialStrategy.nextRetryTime(retryTask));
    }
}
