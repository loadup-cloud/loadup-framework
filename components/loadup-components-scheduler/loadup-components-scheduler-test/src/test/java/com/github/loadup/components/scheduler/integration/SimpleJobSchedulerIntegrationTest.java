/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.integration;

import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import com.github.loadup.components.scheduler.api.SchedulerBinding;
import com.github.loadup.components.scheduler.core.SchedulerTaskRegistry;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration test for SimpleJob scheduler.
 */
@SpringBootTest(classes = SimpleJobSchedulerIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = {
    "loadup.scheduler.type=simplejob"
})
class SimpleJobSchedulerIntegrationTest {

    @Autowired
    private SchedulerTaskRegistry taskRegistry;

    @Autowired
    private SchedulerBinding schedulerBinding;

    @Autowired
    private TestScheduledTasks testTasks;

    @Test
    void testSchedulerAutoConfiguration() {
        // Then
        assertThat(taskRegistry).isNotNull();
        assertThat(schedulerBinding).isNotNull();
    }

    @Test
    void testAnnotationBasedScheduling() {
        // Given - task should be auto-registered by @DistributedScheduler

        // Wait for task execution
        await().atMost(5, SECONDS)
            .until(() -> testTasks.getExecutionCount() > 0);

        // Then
        assertThat(testTasks.getExecutionCount()).isGreaterThan(0);
        assertThat(taskRegistry.findByTaskName("integrationTestTask")).isNotNull();
    }

    @Test
    void testDynamicTaskManagement() {
        // Given
        String taskName = "dynamicTask";
        SchedulerTask task = SchedulerTask.builder()
            .taskName(taskName)
            .cron("*/2 * * * * ?")
            .build();

        // When
        boolean registered = schedulerBinding.registerTask(task);

        // Then
        assertThat(registered).isTrue();
        assertThat(schedulerBinding.taskExists(taskName)).isTrue();

        // Cleanup
        schedulerBinding.unregisterTask(taskName);
    }

    @Test
    void testTaskRegistryOperations() {
        // Given
        SchedulerTask task = SchedulerTask.builder()
            .taskName("registryTask")
            .cron("0 0 12 * * ?")
            .build();

        // When
        taskRegistry.registerTask(task);

        // Then
        assertThat(taskRegistry.containsTask("registryTask")).isTrue();
        assertThat(taskRegistry.findByTaskName("registryTask")).isEqualTo(task);
        assertThat(taskRegistry.getTaskCount()).isGreaterThan(0);

        // Cleanup
        taskRegistry.removeTask("registryTask");
    }

    @Configuration
    static class TestConfiguration {
        @Bean
        public TestScheduledTasks testScheduledTasks() {
            return new TestScheduledTasks();
        }
    }

    @Component
    static class TestScheduledTasks {
        private AtomicInteger executionCount = new AtomicInteger(0);

        @DistributedScheduler(name = "integrationTestTask", cron = "*/2 * * * * ?")
        public void scheduledTask() {
            executionCount.incrementAndGet();
        }

        public int getExecutionCount() {
            return executionCount.get();
        }
    }
}

