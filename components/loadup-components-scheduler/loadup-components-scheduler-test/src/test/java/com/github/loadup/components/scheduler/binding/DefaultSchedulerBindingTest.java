/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.binding;

import com.github.loadup.components.scheduler.api.SchedulerBinder;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DefaultSchedulerBinding.
 */
@ExtendWith(MockitoExtension.class)
class DefaultSchedulerBindingTest {

    @Mock
    private SchedulerBinder schedulerBinder;

    private DefaultSchedulerBinding schedulerBinding;

    @BeforeEach
    void setUp() {
        when(schedulerBinder.getName()).thenReturn("test-binder");
        schedulerBinding = new DefaultSchedulerBinding(schedulerBinder);
    }

    @Test
    void testRegisterTask() {
        // Given
        SchedulerTask task = SchedulerTask.builder()
            .taskName("testTask")
            .cron("0 0 12 * * ?")
            .build();
        when(schedulerBinder.registerTask(task)).thenReturn(true);

        // When
        boolean result = schedulerBinding.registerTask(task);

        // Then
        assertThat(result).isTrue();
        verify(schedulerBinder).registerTask(task);
    }

    @Test
    void testUnregisterTask() {
        // Given
        String taskName = "testTask";
        when(schedulerBinder.unregisterTask(taskName)).thenReturn(true);

        // When
        boolean result = schedulerBinding.unregisterTask(taskName);

        // Then
        assertThat(result).isTrue();
        verify(schedulerBinder).unregisterTask(taskName);
    }

    @Test
    void testPauseTask() {
        // Given
        String taskName = "testTask";
        when(schedulerBinder.pauseTask(taskName)).thenReturn(true);

        // When
        boolean result = schedulerBinding.pauseTask(taskName);

        // Then
        assertThat(result).isTrue();
        verify(schedulerBinder).pauseTask(taskName);
    }

    @Test
    void testResumeTask() {
        // Given
        String taskName = "testTask";
        when(schedulerBinder.resumeTask(taskName)).thenReturn(true);

        // When
        boolean result = schedulerBinding.resumeTask(taskName);

        // Then
        assertThat(result).isTrue();
        verify(schedulerBinder).resumeTask(taskName);
    }

    @Test
    void testTriggerTask() {
        // Given
        String taskName = "testTask";
        when(schedulerBinder.triggerTask(taskName)).thenReturn(true);

        // When
        boolean result = schedulerBinding.triggerTask(taskName);

        // Then
        assertThat(result).isTrue();
        verify(schedulerBinder).triggerTask(taskName);
    }

    @Test
    void testUpdateTaskCron() {
        // Given
        String taskName = "testTask";
        String newCron = "0 0 13 * * ?";
        when(schedulerBinder.updateTaskCron(taskName, newCron)).thenReturn(true);

        // When
        boolean result = schedulerBinding.updateTaskCron(taskName, newCron);

        // Then
        assertThat(result).isTrue();
        verify(schedulerBinder).updateTaskCron(taskName, newCron);
    }

    @Test
    void testTaskExists() {
        // Given
        String taskName = "testTask";
        when(schedulerBinder.taskExists(taskName)).thenReturn(true);

        // When
        boolean result = schedulerBinding.taskExists(taskName);

        // Then
        assertThat(result).isTrue();
        verify(schedulerBinder).taskExists(taskName);
    }

    @Test
    void testInit() {
        // When
        schedulerBinding.init();

        // Then
        verify(schedulerBinder).init();
    }

    @Test
    void testDestroy() {
        // When
        schedulerBinding.destroy();

        // Then
        verify(schedulerBinder).destroy();
    }
}

