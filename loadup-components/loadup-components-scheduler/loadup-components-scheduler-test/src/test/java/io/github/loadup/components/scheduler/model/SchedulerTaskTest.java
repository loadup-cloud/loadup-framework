package io.github.loadup.components.scheduler.model;

/*-
 * #%L
 * loadup-components-scheduler-test
 * %%
 * Copyright (C) 2025 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

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
                .timeoutMillis(5000L)
                .maxRetries(3)
                .build();

        // Then
        assertThat(task.getTaskName()).isEqualTo(taskName);
        assertThat(task.getCron()).isEqualTo(cron);
        assertThat(task.getDescription()).isEqualTo(description);
        assertThat(task.getTaskGroup()).isEqualTo(taskGroup);
        assertThat(task.isEnabled()).isTrue();
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
        assertThat(task.getTimeoutMillis()).isEqualTo(0L);
        assertThat(task.getMaxRetries()).isEqualTo(0);
    }

    @Test
    void testSchedulerTaskWithMethodAndBean() throws NoSuchMethodException {
        // Given
        Method method = SchedulerTaskTest.class.getMethod("helperMethod");
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

    // Helper method for testing
    public void helperMethod() {
        // Test helper
    }

    @Test
    void testSchedulerTaskWithParameters() {
        // Given
        Object[] params = new Object[0];

        // When
        SchedulerTask task = SchedulerTask.builder()
                .taskName("paramTask")
                .cron("0 0 12 * * ?")
                .args(params)
                .build();

        // Then
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
        task.setTimeoutMillis(10000L);
        task.setMaxRetries(5);

        // Then
        assertThat(task.getTaskName()).isEqualTo("newTask");
        assertThat(task.getCron()).isEqualTo("0 0 13 * * ?");
        assertThat(task.getDescription()).isEqualTo("New description");
        assertThat(task.getTaskGroup()).isEqualTo("newGroup");
        assertThat(task.isEnabled()).isFalse();
        assertThat(task.getTimeoutMillis()).isEqualTo(10000L);
        assertThat(task.getMaxRetries()).isEqualTo(5);
    }

    @Test
    void testSchedulerTaskEqualsAndHashCode() {
        // Given
        SchedulerTask task1 =
                SchedulerTask.builder().taskName("task").cron("0 0 12 * * ?").build();

        SchedulerTask task2 =
                SchedulerTask.builder().taskName("task").cron("0 0 12 * * ?").build();

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
