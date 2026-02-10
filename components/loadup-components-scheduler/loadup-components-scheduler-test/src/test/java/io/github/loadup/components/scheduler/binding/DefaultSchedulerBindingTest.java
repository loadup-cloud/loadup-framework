package io.github.loadup.components.scheduler.binding;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.components.scheduler.binder.SchedulerBinder;
import io.github.loadup.components.scheduler.model.SchedulerTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for DefaultSchedulerBinding. */
@ExtendWith(MockitoExtension.class)
class DefaultSchedulerBindingTest {

    @Mock
    private SchedulerBinder schedulerBinder;

    private DefaultSchedulerBinding schedulerBinding;

    @BeforeEach
    void setUp() {
        schedulerBinding = new DefaultSchedulerBinding();
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
}
