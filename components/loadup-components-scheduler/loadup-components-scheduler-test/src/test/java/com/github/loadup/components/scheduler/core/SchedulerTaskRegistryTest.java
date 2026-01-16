package com.github.loadup.components.scheduler.core;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import com.github.loadup.components.scheduler.binding.SchedulerBinding;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import java.util.Collection;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeansException;

/**
 * Unit tests for SchedulerTaskRegistry.
 */
@ExtendWith(MockitoExtension.class)
class SchedulerTaskRegistryTest {

    @Mock
    private SchedulerBinding schedulerBinding;

    private SchedulerTaskRegistry registry;

    @BeforeEach
    void setUp() {
        // Reset the mock to clear any previous interactions
        reset(schedulerBinding);

        registry = new SchedulerTaskRegistry();
        // Use reflection to set the schedulerBinding field
        try {
            java.lang.reflect.Field field = SchedulerTaskRegistry.class.getDeclaredField("schedulerBinding");
            field.setAccessible(true);
            field.set(registry, schedulerBinding);

            // Clear the static registry to ensure test isolation
            java.lang.reflect.Field registryField = SchedulerTaskRegistry.class.getDeclaredField("TASK_REGISTRY");
            registryField.setAccessible(true);
            Map<String, SchedulerTask> taskRegistry = (Map<String, SchedulerTask>) registryField.get(null);
            taskRegistry.clear();

            // Clear the static pending tasks to ensure test isolation
            java.lang.reflect.Field pendingField = SchedulerTaskRegistry.class.getDeclaredField("PENDING_TASKS");
            pendingField.setAccessible(true);
            Map<String, SchedulerTask> pendingTasks = (Map<String, SchedulerTask>) pendingField.get(null);
            pendingTasks.clear();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testPostProcessAfterInitialization_WithDistributedSchedulerAnnotation() throws BeansException {
        // Given
        TestBean testBean = new TestBean();
        when(schedulerBinding.registerTask(any(SchedulerTask.class))).thenReturn(true);

        // When
        Object result = registry.postProcessAfterInitialization(testBean, "testBean");

        // Then
        assertThat(result).isSameAs(testBean);

        // Verify task is registered in local registry
        SchedulerTask task = registry.findByTaskName("testTask");
        assertThat(task).isNotNull();
        assertThat(task.getTaskName()).isEqualTo("testTask");
        assertThat(task.getCron()).isEqualTo("0 0 12 * * ?");
        assertThat(task.getMethod().getName()).isEqualTo("scheduledMethod");
        assertThat(task.getTargetBean()).isSameAs(testBean);

        // Verify schedulerBinding.registerTask is NOT called yet (delayed until ContextRefreshedEvent)
        verify(schedulerBinding, never()).registerTask(any(SchedulerTask.class));

        // Simulate context refresh event
        registry.onApplicationEvent(mock(org.springframework.context.event.ContextRefreshedEvent.class));

        // Now verify schedulerBinding.registerTask was called
        ArgumentCaptor<SchedulerTask> taskCaptor = ArgumentCaptor.forClass(SchedulerTask.class);
        verify(schedulerBinding).registerTask(taskCaptor.capture());

        SchedulerTask capturedTask = taskCaptor.getValue();
        assertThat(capturedTask.getTaskName()).isEqualTo("testTask");
    }

    @Test
    void testPostProcessAfterInitialization_WithEmptyTaskName() throws BeansException {
        // Given
        TestBeanWithEmptyName testBean = new TestBeanWithEmptyName();
        when(schedulerBinding.registerTask(any(SchedulerTask.class))).thenReturn(true);

        // When
        Object result = registry.postProcessAfterInitialization(testBean, "testBean");

        // Then
        assertThat(result).isSameAs(testBean);

        // Verify task is registered in local registry with generated name (beanName.methodName)
        SchedulerTask task = registry.findByTaskName("testBean.scheduledMethod");
        assertThat(task).isNotNull();
        assertThat(task.getTaskName()).isEqualTo("testBean.scheduledMethod");

        // Verify schedulerBinding.registerTask is NOT called yet (delayed until ContextRefreshedEvent)
        verify(schedulerBinding, never()).registerTask(any(SchedulerTask.class));

        // Simulate context refresh event
        registry.onApplicationEvent(mock(org.springframework.context.event.ContextRefreshedEvent.class));

        // Now verify schedulerBinding.registerTask was called
        ArgumentCaptor<SchedulerTask> taskCaptor = ArgumentCaptor.forClass(SchedulerTask.class);
        verify(schedulerBinding).registerTask(taskCaptor.capture());

        SchedulerTask capturedTask = taskCaptor.getValue();
        assertThat(capturedTask.getTaskName()).isEqualTo("testBean.scheduledMethod");
    }

    @Test
    void testPostProcessAfterInitialization_WithoutSchedulerBinding() throws BeansException {
        // Given
        SchedulerTaskRegistry registryWithoutBinding = new SchedulerTaskRegistry();
        TestBean testBean = new TestBean();

        // When
        Object result = registryWithoutBinding.postProcessAfterInitialization(testBean, "testBean");

        // Then
        assertThat(result).isSameAs(testBean);
        // No exception should be thrown
    }

    @Test
    void testPostProcessAfterInitialization_WithoutAnnotation() throws BeansException {
        // Given
        Object plainBean = new Object();

        // When
        Object result = registry.postProcessAfterInitialization(plainBean, "plainBean");

        // Then
        assertThat(result).isSameAs(plainBean);
        verify(schedulerBinding, never()).registerTask(any());
    }

    @Test
    void testRegisterTask() {
        // Given
        SchedulerTask task = SchedulerTask.builder()
            .taskName("manualTask")
            .cron("0 0 13 * * ?")
            .build();

        // When
        registry.registerTask(task);

        // Then
        assertThat(registry.findByTaskName("manualTask")).isEqualTo(task);
    }

    @Test
    void testRegisterTask_DuplicateName() {
        // Given
        SchedulerTask task1 = SchedulerTask.builder()
            .taskName("duplicateTask")
            .cron("0 0 12 * * ?")
            .build();
        SchedulerTask task2 = SchedulerTask.builder()
            .taskName("duplicateTask")
            .cron("0 0 13 * * ?")
            .build();

        // When
        registry.registerTask(task1);
        registry.registerTask(task2);

        // Then - task2 should overwrite task1
        assertThat(registry.findByTaskName("duplicateTask")).isEqualTo(task2);
    }

    @Test
    void testFindByTaskName() {
        // Given
        SchedulerTask task = SchedulerTask.builder()
            .taskName("findTask")
            .cron("0 0 12 * * ?")
            .build();
        registry.registerTask(task);

        // When
        SchedulerTask found = registry.findByTaskName("findTask");

        // Then
        assertThat(found).isEqualTo(task);
    }

    @Test
    void testFindByTaskName_NotFound() {
        // When
        SchedulerTask found = registry.findByTaskName("nonExistent");

        // Then
        assertThat(found).isNull();
    }

    @Test
    void testFindAllTasks() {
        // Given
        SchedulerTask task1 = SchedulerTask.builder()
            .taskName("task1")
            .cron("0 0 12 * * ?")
            .build();
        SchedulerTask task2 = SchedulerTask.builder()
            .taskName("task2")
            .cron("0 0 13 * * ?")
            .build();
        registry.registerTask(task1);
        registry.registerTask(task2);

        // When
        Collection<SchedulerTask> allTasks = registry.findAllTasks();

        // Then
        assertThat(allTasks).hasSize(2);
        assertThat(allTasks).contains(task1, task2);
    }

    @Test
    void testGetTaskRegistry() {
        // Given
        SchedulerTask task = SchedulerTask.builder()
            .taskName("registryTask")
            .cron("0 0 12 * * ?")
            .build();
        registry.registerTask(task);

        // When
        Map<String, SchedulerTask> taskRegistry = registry.getTaskRegistry();

        // Then
        assertThat(taskRegistry).hasSize(1);
        assertThat(taskRegistry.get("registryTask")).isEqualTo(task);
    }

    @Test
    void testRemoveTask() {
        // Given
        SchedulerTask task = SchedulerTask.builder()
            .taskName("removeTask")
            .cron("0 0 12 * * ?")
            .build();
        registry.registerTask(task);

        // When
        SchedulerTask removed = registry.removeTask("removeTask");

        // Then
        assertThat(removed).isEqualTo(task);
        assertThat(registry.findByTaskName("removeTask")).isNull();
    }

    @Test
    void testRemoveTask_NotFound() {
        // When
        SchedulerTask removed = registry.removeTask("nonExistent");

        // Then
        assertThat(removed).isNull();
    }

    @Test
    void testContainsTask() {
        // Given
        SchedulerTask task = SchedulerTask.builder()
            .taskName("containsTask")
            .cron("0 0 12 * * ?")
            .build();
        registry.registerTask(task);

        // When/Then
        assertThat(registry.containsTask("containsTask")).isTrue();
        assertThat(registry.containsTask("nonExistent")).isFalse();
    }

    @Test
    void testGetTaskCount() {
        // Given
        registry.registerTask(SchedulerTask.builder().taskName("task1").cron("0 0 12 * * ?").build());
        registry.registerTask(SchedulerTask.builder().taskName("task2").cron("0 0 13 * * ?").build());

        // When
        int count = registry.getTaskCount();

        // Then
        assertThat(count).isEqualTo(2);
    }

    // Test beans
    static class TestBean {
        @DistributedScheduler(name = "testTask", cron = "0 0 12 * * ?")
        public void scheduledMethod() {
            // Test method
        }
    }

    static class TestBeanWithEmptyName {
        @DistributedScheduler(name = "", cron = "0 0 12 * * ?")
        public void scheduledMethod() {
            // Test method
        }
    }
}

