package com.github.loadup.components.scheduler.api;

/*-
 * #%L
 * loadup-components-scheduler-test
 * %%
 * Copyright (C) 2025 LoadUp Cloud
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

