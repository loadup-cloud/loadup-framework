/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.api;

import com.github.loadup.components.scheduler.model.SchedulerTask;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SchedulerBinder interface default methods.
 */
class SchedulerBinderTest {

    @Test
    void testDefaultRegisterTask() {
        // Given
        TestSchedulerBinder binder = new TestSchedulerBinder();
        SchedulerTask task = SchedulerTask.builder()
            .taskName("test")
            .cron("0 0 12 * * ?")
            .build();

        // When
        boolean result = binder.registerTask(task);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testDefaultUnregisterTask() {
        // Given
        TestSchedulerBinder binder = new TestSchedulerBinder();

        // When
        boolean result = binder.unregisterTask("test");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testDefaultPauseTask() {
        // Given
        TestSchedulerBinder binder = new TestSchedulerBinder();

        // When
        boolean result = binder.pauseTask("test");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testDefaultResumeTask() {
        // Given
        TestSchedulerBinder binder = new TestSchedulerBinder();

        // When
        boolean result = binder.resumeTask("test");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testDefaultTriggerTask() {
        // Given
        TestSchedulerBinder binder = new TestSchedulerBinder();

        // When
        boolean result = binder.triggerTask("test");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testDefaultUpdateTaskCron() {
        // Given
        TestSchedulerBinder binder = new TestSchedulerBinder();

        // When
        boolean result = binder.updateTaskCron("test", "0 0 13 * * ?");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testDefaultTaskExists() {
        // Given
        TestSchedulerBinder binder = new TestSchedulerBinder();

        // When
        boolean result = binder.taskExists("test");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testLifecycleMethods() {
        // Given
        TestSchedulerBinder binder = new TestSchedulerBinder();

        // When/Then - should not throw exceptions
        binder.init();
        binder.postProcessAfterInstantiation();
        binder.destroy();
    }

    // Test implementation using default methods
    static class TestSchedulerBinder implements SchedulerBinder {
        @Override
        public String getName() {
            return "test-binder";
        }
    }
}

