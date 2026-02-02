package io.github.loadup.components.scheduler.integration;

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

import io.github.loadup.components.scheduler.annotation.DistributedScheduler;
import io.github.loadup.components.scheduler.binding.SchedulerBinding;
import io.github.loadup.components.scheduler.model.SchedulerTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

/** Integration test for Quartz scheduler. */
@SpringBootTest(classes = QuartzSchedulerIntegrationTest.TestConfiguration.class)
@TestPropertySource(properties = {"loadup.scheduler.type=quartz", "spring.quartz.job-store-type=memory"})
class QuartzSchedulerIntegrationTest {

    @Autowired
    private SchedulerBinding schedulerBinding;

    @Autowired
    private TestScheduledTasks testTasks;

    @Test
    void testSchedulerAutoConfiguration() {
        // Then
        assertThat(schedulerBinding).isNotNull();
    }

    @Test
    void testAnnotationBasedScheduling() {
        // Wait for task execution
        await().atMost(5, SECONDS).until(() -> testTasks.getExecutionCount() > 0);

        // Then
        assertThat(testTasks.getExecutionCount()).isGreaterThan(0);
    }

    @Test
    void testDynamicTaskManagement() {
        // Given
        String taskName = "dynamicQuartzTask";
        SchedulerTask task =
                SchedulerTask.builder().taskName(taskName).cron("0 0 12 * * ?").build();

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
        SchedulerTask task =
                SchedulerTask.builder().taskName(taskName).cron("0 0 12 * * ?").build();
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
    @EnableAutoConfiguration
    static class TestConfiguration {
        @Bean
        public TestScheduledTasks testScheduledTasks() {
            return new TestScheduledTasks();
        }
    }

    public static class TestScheduledTasks {
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
