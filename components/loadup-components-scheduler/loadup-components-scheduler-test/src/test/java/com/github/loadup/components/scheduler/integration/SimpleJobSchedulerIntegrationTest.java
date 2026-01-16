package com.github.loadup.components.scheduler.integration;

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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import com.github.loadup.components.scheduler.binding.SchedulerBinding;
import com.github.loadup.components.scheduler.core.SchedulerTaskRegistry;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.TestPropertySource;

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
    @EnableAutoConfiguration
    static class TestConfiguration {
        @Bean
        public TestScheduledTasks testScheduledTasks() {
            return new TestScheduledTasks();
        }

        @Bean
        public TaskScheduler taskScheduler() {
            ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
            scheduler.setPoolSize(10);
            scheduler.setThreadNamePrefix("scheduler-");
            scheduler.initialize();
            return scheduler;
        }
    }

    public static class TestScheduledTasks {
        private final AtomicInteger executionCount = new AtomicInteger(0);

        @DistributedScheduler(name = "integrationTestTask", cron = "*/2 * * * * ?")
        public void scheduledTask() {
            executionCount.incrementAndGet();
        }

        public int getExecutionCount() {
            return executionCount.get();
        }
    }
}

