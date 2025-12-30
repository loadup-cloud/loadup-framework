/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SchedulerTask model.
 */
class SchedulerTaskTest {

    @Test
    void testSchedulerTaskBuilder() {
        // Given
        String taskName = "testTask";
        String cron = "0 0 12 * * ?";
        String description = "Test task description";
        String taskGroup = "testGroup";

        // When
        SchedulerTask task = SchedulerTask.builder()
            .taskName(taskName)
            .cron(cron)
            .description(description)
            .taskGroup(taskGroup)
            .enabled(true)
            .priority(5)
            .timeoutMillis(5000L)
            .maxRetries(3)
            .build();

        // Then
        assertThat(task.getTaskName()).isEqualTo(taskName);
        assertThat(task.getCron()).isEqualTo(cron);
        assertThat(task.getDescription()).isEqualTo(description);
        assertThat(task.getTaskGroup()).isEqualTo(taskGroup);
        assertThat(task.isEnabled()).isTrue();
        assertThat(task.getPriority()).isEqualTo(5);
        assertThat(task.getTimeoutMillis()).isEqualTo(5000L);
        assertThat(task.getMaxRetries()).isEqualTo(3);
    }

    @Test
    void testSchedulerTaskDefaultValues() {
        // When
        SchedulerTask task = SchedulerTask.builder()
            .taskName("testTask")
            .cron("0 0 12 * * ?")
            .build();

        // Then
        assertThat(task.isEnabled()).isTrue();
        assertThat(task.getPriority()).isEqualTo(0);
        assertThat(task.getTimeoutMillis()).isEqualTo(0L);
        assertThat(task.getMaxRetries()).isEqualTo(0);
    }

    @Test
    void testSchedulerTaskWithMethodAndBean() throws NoSuchMethodException {
        // Given
        Method method = this.getClass().getMethod("testSchedulerTaskWithMethodAndBean");
        Object bean = this;

        // When
        SchedulerTask task = SchedulerTask.builder()
            .taskName("methodTask")
            .cron("0 0 12 * * ?")
            .method(method)
            .targetBean(bean)
            .build();

        // Then
        assertThat(task.getMethod()).isEqualTo(method);
        assertThat(task.getTargetBean()).isEqualTo(bean);
    }

    @Test
    void testSchedulerTaskWithParameters() {
        // Given
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("key1", "value1");
        params.put("key2", 123);

        // When
        SchedulerTask task = SchedulerTask.builder()
            .taskName("paramTask")
            .cron("0 0 12 * * ?")
            .parameters(params)
            .build();

        // Then
        assertThat(task.getParameters()).isNotNull();
        assertThat(task.getParameters()).hasSize(2);
        assertThat(task.getParameters().get("key1")).isEqualTo("value1");
        assertThat(task.getParameters().get("key2")).isEqualTo(123);
    }

    @Test
    void testSchedulerTaskSettersAndGetters() {
        // Given
        SchedulerTask task = new SchedulerTask();

        // When
        task.setTaskName("newTask");
        task.setCron("0 0 13 * * ?");
        task.setDescription("New description");
        task.setTaskGroup("newGroup");
        task.setEnabled(false);
        task.setPriority(10);
        task.setTimeoutMillis(10000L);
        task.setMaxRetries(5);

        // Then
        assertThat(task.getTaskName()).isEqualTo("newTask");
        assertThat(task.getCron()).isEqualTo("0 0 13 * * ?");
        assertThat(task.getDescription()).isEqualTo("New description");
        assertThat(task.getTaskGroup()).isEqualTo("newGroup");
        assertThat(task.isEnabled()).isFalse();
        assertThat(task.getPriority()).isEqualTo(10);
        assertThat(task.getTimeoutMillis()).isEqualTo(10000L);
        assertThat(task.getMaxRetries()).isEqualTo(5);
    }

    @Test
    void testSchedulerTaskEqualsAndHashCode() {
        // Given
        SchedulerTask task1 = SchedulerTask.builder()
            .taskName("task")
            .cron("0 0 12 * * ?")
            .build();

        SchedulerTask task2 = SchedulerTask.builder()
            .taskName("task")
            .cron("0 0 12 * * ?")
            .build();

        // Then
        assertThat(task1).isEqualTo(task2);
        assertThat(task1.hashCode()).isEqualTo(task2.hashCode());
    }

    @Test
    void testSchedulerTaskToString() {
        // Given
        SchedulerTask task = SchedulerTask.builder()
            .taskName("testTask")
            .cron("0 0 12 * * ?")
            .description("Test")
            .build();

        // When
        String toString = task.toString();

        // Then
        assertThat(toString).contains("testTask");
        assertThat(toString).contains("0 0 12 * * ?");
    }
}

