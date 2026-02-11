package io.github.loadup.retrytask.test.strategy;

import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.strategy.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RetryStrategyTest {

    @Test
    @DisplayName("Fixed Strategy should return constant delay")
    void testFixedStrategy() {
        FixedRetryStrategy strategy = new FixedRetryStrategy();
        RetryTask task = new RetryTask();
        task.setRetryCount(1);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = strategy.nextRetryTime(task);

        long seconds = ChronoUnit.SECONDS.between(now, next);
        // Default is 60s, allow small delta
        assertThat(seconds).isBetween(59L, 61L);
    }

    @Test
    @DisplayName("Exponential Strategy should return exponential delay")
    void testExponentialStrategy() {
        ExponentialBackoffRetryStrategy strategy = new ExponentialBackoffRetryStrategy();
        RetryTask task = new RetryTask();

        // Retry 1: 2^1 * 60 = 120s
        task.setRetryCount(1);
        LocalDateTime next1 = strategy.nextRetryTime(task);
        long seconds1 = ChronoUnit.SECONDS.between(LocalDateTime.now(), next1);
        assertThat(seconds1).isBetween(119L, 121L);

        // Retry 2: 2^2 * 60 = 240s
        task.setRetryCount(2);
        LocalDateTime next2 = strategy.nextRetryTime(task);
        long seconds2 = ChronoUnit.SECONDS.between(LocalDateTime.now(), next2);
        assertThat(seconds2).isBetween(239L, 241L);
    }

    @Test
    @DisplayName("Random Strategy should return delay within range")
    void testRandomStrategy() {
        RandomWaitRetryStrategy strategy = new RandomWaitRetryStrategy();
        RetryTask task = new RetryTask();
        task.setRetryCount(1);

        LocalDateTime next = strategy.nextRetryTime(task);
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), next);

        // Default range 10s - 300s
        assertThat(seconds).isBetween(10L, 300L);
    }

    @Test
    @DisplayName("Incremental Strategy should return incrementally increasing delay")
    void testIncrementalStrategy() {
        IncrementalWaitRetryStrategy strategy = new IncrementalWaitRetryStrategy();
        RetryTask task = new RetryTask();

        // Default: 30s base, formula: base * (count + 1)

        // Retry 1: 30 * (1+1) = 60s
        task.setRetryCount(1);
        LocalDateTime next1 = strategy.nextRetryTime(task);
        long seconds1 = ChronoUnit.SECONDS.between(LocalDateTime.now(), next1);
        assertThat(seconds1).isBetween(59L, 61L);

        // Retry 2: 30 * (2+1) = 90s
        task.setRetryCount(2);
        LocalDateTime next2 = strategy.nextRetryTime(task);
        long seconds2 = ChronoUnit.SECONDS.between(LocalDateTime.now(), next2);
        assertThat(seconds2).isBetween(89L, 91L);
    }

    @Test
    @DisplayName("Fibonacci Strategy should return fibonacci delay")
    void testFibonacciStrategy() {
        FibonacciBackoffRetryStrategy strategy = new FibonacciBackoffRetryStrategy();
        RetryTask task = new RetryTask();

        // Default multiplier 1s
        // Fib: 1, 1, 2, 3, 5, 8...

        // Retry 1: Fib(1) * 1 = 1s
        task.setRetryCount(1);
        long seconds1 = ChronoUnit.SECONDS.between(LocalDateTime.now(), strategy.nextRetryTime(task));
        assertThat(seconds1).isBetween(0L, 2L);

        // Retry 3: Fib(3) * 1 = 2 * 1 = 2s
        task.setRetryCount(3);
        long seconds3 = ChronoUnit.SECONDS.between(LocalDateTime.now(), strategy.nextRetryTime(task));
        assertThat(seconds3).isBetween(1L, 3L);
    }
}

