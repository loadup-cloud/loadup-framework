package io.github.loadup.retrytask.test.core;

/*-
 * #%L
 * Loadup Components Retrytask Test
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

import static org.junit.jupiter.api.Assertions.*;

import io.github.loadup.retrytask.facade.enums.RetryTaskStatus;
import io.github.loadup.retrytask.facade.model.RetryTask;
import io.github.loadup.retrytask.test.BaseRetryTaskTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RetryTask model
 */
class RetryTaskTest extends BaseRetryTaskTest {

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
