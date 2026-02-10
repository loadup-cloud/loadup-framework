package io.github.loadup.retrytask.test.core;

import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.facade.model.RetryTaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RetryTask model
 */
class RetryTaskTest {

    private RetryTask retryTask;

    @BeforeEach
    void setUp() {
        retryTask = new RetryTask();
        retryTask.setId(1L);
        retryTask.setBizType("ORDER");
        retryTask.setBizId("12345");
        retryTask.setMaxRetryCount(3);
        retryTask.setRetryCount(0);
        retryTask.setStatus(RetryTaskStatus.PENDING);
        retryTask.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create retry task with valid properties")
    void testRetryTaskCreation() {
        // Then
        assertNotNull(retryTask);
        assertEquals(1L, retryTask.getId());
        assertEquals("ORDER", retryTask.getBizType());
        assertEquals("12345", retryTask.getBizId());
        assertEquals(3, retryTask.getMaxRetryCount());
        assertEquals(0, retryTask.getRetryCount());
        assertEquals(RetryTaskStatus.PENDING, retryTask.getStatus());
        assertNotNull(retryTask.getCreateTime());
    }

    @Test
    @DisplayName("Should increment retry count correctly")
    void testIncrementRetryCount() {
        // Given
        int initialRetryCount = retryTask.getRetryCount();

        // When
        retryTask.setRetryCount(retryTask.getRetryCount() + 1);

        // Then
        assertEquals(initialRetryCount + 1, retryTask.getRetryCount());
    }

    @Test
    @DisplayName("Should determine if max retries exceeded")
    void testMaxRetriesExceeded() {
        // Given
        retryTask.setRetryCount(3);
        retryTask.setMaxRetryCount(3);

        // When
        boolean maxRetriesExceeded = retryTask.getRetryCount() >= retryTask.getMaxRetryCount();

        // Then
        assertTrue(maxRetriesExceeded);
    }

    @Test
    @DisplayName("Should determine if max retries not exceeded")
    void testMaxRetriesNotExceeded() {
        // Given
        retryTask.setRetryCount(2);
        retryTask.setMaxRetryCount(3);

        // When
        boolean maxRetriesExceeded = retryTask.getRetryCount() >= retryTask.getMaxRetryCount();

        // Then
        assertFalse(maxRetriesExceeded);
    }

    @Test
    @DisplayName("Should update status correctly")
    void testStatusUpdate() {
        // When
        retryTask.setStatus(RetryTaskStatus.SUCCESS);

        // Then
        assertEquals(RetryTaskStatus.SUCCESS, retryTask.getStatus());
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void testNullValues() {
        // Given
        RetryTask nullTask = new RetryTask();

        // Then
        assertNull(nullTask.getId());
        assertNull(nullTask.getBizType());
        assertNull(nullTask.getBizId());
        assertNull(nullTask.getStatus());
        assertNull(nullTask.getCreateTime());
    }

    @Test
    @DisplayName("Should handle edge cases for retry counts")
    void testRetryCountEdgeCases() {
        // Test with zero max retry count
        retryTask.setMaxRetryCount(0);
        retryTask.setRetryCount(0);
        assertTrue(retryTask.getRetryCount() >= retryTask.getMaxRetryCount());

        // Test with negative values (should be handled by validation)
        retryTask.setMaxRetryCount(-1);
        retryTask.setRetryCount(-1);
        assertTrue(retryTask.getRetryCount() >= retryTask.getMaxRetryCount());
    }
}
