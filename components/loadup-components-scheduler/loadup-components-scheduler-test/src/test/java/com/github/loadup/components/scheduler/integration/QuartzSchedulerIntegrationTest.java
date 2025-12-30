/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.integration;

import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import com.github.loadup.components.scheduler.api.SchedulerBinding;
import com.github.loadup.components.scheduler.config.SchedulerAutoConfiguration;
import com.github.loadup.components.scheduler.core.SchedulerTaskRegistry;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import com.github.loadup.components.scheduler.quartz.config.QuartzSchedulerBinderAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.*;
import org.springframework.test.context.TestPropertySource;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration test for Quartz scheduler.
 */
@SpringBootTest(classes = QuartzSchedulerIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = {
    "loadup.scheduler.type=quartz",
    "spring.quartz.job-store-type=memory"
})
class QuartzSchedulerIntegrationTest {

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
        // Wait for task execution
        await().atMost(5, SECONDS)
            .until(() -> testTasks.getExecutionCount() > 0);

        // Then
        assertThat(testTasks.getExecutionCount()).isGreaterThan(0);
        assertThat(taskRegistry.findByTaskName("quartzTestTask")).isNotNull();
    }

    @Test
    void testDynamicTaskManagement() {
        // Given
        String taskName = "dynamicQuartzTask";
        SchedulerTask task = SchedulerTask.builder()
            .taskName(taskName)
            .cron("0 0 12 * * ?")
            .build();

        // When
        boolean registered = schedulerBinding.registerTask(task);

        // Then
        assertThat(registered).isTrue();
        assertThat(schedulerBinding.taskExists(taskName)).isTrue();

        // Test pause/resume
        boolean paused = schedulerBinding.pauseTask(taskName);
        assertThat(paused).isTrue();

        boolean resumed = schedulerBinding.resumeTask(taskName);
        assertThat(resumed).isTrue();

        // Cleanup
        schedulerBinding.unregisterTask(taskName);
        assertThat(schedulerBinding.taskExists(taskName)).isFalse();
    }

    @Test
    void testUpdateTaskCron() {
        // Given
        String taskName = "updateCronTask";
        SchedulerTask task = SchedulerTask.builder()
            .taskName(taskName)
            .cron("0 0 12 * * ?")
            .build();
        schedulerBinding.registerTask(task);

        // When
        boolean updated = schedulerBinding.updateTaskCron(taskName, "0 0 13 * * ?");

        // Then
        assertThat(updated).isTrue();

        // Cleanup
        schedulerBinding.unregisterTask(taskName);
    }

    @Test
    void testManualTrigger() {
        // Given
        String taskName = "triggerTask";
        SchedulerTask task = SchedulerTask.builder()
            .taskName(taskName)
            .cron("0 0 12 * * ?") // Won't run naturally
            .build();
        schedulerBinding.registerTask(task);

        // When
        boolean triggered = schedulerBinding.triggerTask(taskName);

        // Then
        assertThat(triggered).isTrue();

        // Cleanup
        schedulerBinding.unregisterTask(taskName);
    }

    @Configuration
    @Import({SchedulerAutoConfiguration.class, QuartzSchedulerBinderAutoConfiguration.class})
    static class TestConfiguration {
        @Bean
        public TestScheduledTasks testScheduledTasks() {
            return new TestScheduledTasks();
        }
    }

    static class TestScheduledTasks {
        private final AtomicInteger executionCount = new AtomicInteger(0);

        @DistributedScheduler(name = "quartzTestTask", cron = "*/2 * * * * ?")
        public void scheduledTask() {
            executionCount.incrementAndGet();
        }

        public int getExecutionCount() {
            return executionCount.get();
        }
    }
}

