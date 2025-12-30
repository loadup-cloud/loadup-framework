/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.simplejob;

import com.github.loadup.components.scheduler.model.SchedulerTask;
import org.junit.jupiter.api.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Unit tests for SimpleJobSchedulerBinder.
 */
class SimpleJobSchedulerBinderTest {

    private SimpleJobSchedulerBinder binder;
    private TaskScheduler            taskScheduler;
    private TestTaskExecutor         testExecutor;

    @BeforeEach
    void setUp() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("test-");
        scheduler.initialize();
        taskScheduler = scheduler;

        binder = new SimpleJobSchedulerBinder(taskScheduler);
        binder.init();

        testExecutor = new TestTaskExecutor();
    }

    @AfterEach
    void tearDown() {
        if (binder != null) {
            binder.destroy();
        }
    }

    @Test
    void testGetName() {
        // When
        String name = binder.getName();

        // Then
        assertThat(name).isEqualTo("simplejob");
    }

    @Test
    void testRegisterTask() throws Exception {
        // Given
        Method method = TestTaskExecutor.class.getMethod("execute");
        SchedulerTask task = SchedulerTask.builder()
            .taskName("testTask")
            .cron("*/1 * * * * ?") // Every second
            .method(method)
            .targetBean(testExecutor)
            .build();

        // When
        boolean result = binder.registerTask(task);

        // Then
        assertThat(result).isTrue();
        assertThat(binder.taskExists("testTask")).isTrue();

        // Wait for task execution
        await().atMost(3, TimeUnit.SECONDS)
            .until(() -> testExecutor.getExecutionCount() > 0);

        assertThat(testExecutor.getExecutionCount()).isGreaterThan(0);
    }

    @Test
    void testRegisterTask_DuplicateTask() throws Exception {
        // Given
        Method method = TestTaskExecutor.class.getMethod("execute");
        SchedulerTask task = SchedulerTask.builder()
            .taskName("duplicateTask")
            .cron("*/1 * * * * ?")
            .method(method)
            .targetBean(testExecutor)
            .build();

        // When
        boolean result1 = binder.registerTask(task);
        boolean result2 = binder.registerTask(task);

        // Then
        assertThat(result1).isTrue();
        assertThat(result2).isTrue();
        assertThat(binder.taskExists("duplicateTask")).isTrue();
    }

    @Test
    void testUnregisterTask() throws Exception {
        // Given
        Method method = TestTaskExecutor.class.getMethod("execute");
        SchedulerTask task = SchedulerTask.builder()
            .taskName("unregisterTask")
            .cron("*/1 * * * * ?")
            .method(method)
            .targetBean(testExecutor)
            .build();
        binder.registerTask(task);

        // When
        boolean result = binder.unregisterTask("unregisterTask");

        // Then
        assertThat(result).isTrue();
        assertThat(binder.taskExists("unregisterTask")).isFalse();
    }

    @Test
    void testUnregisterTask_NotFound() {
        // When
        boolean result = binder.unregisterTask("nonExistent");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testPauseTask() {
        // When
        boolean result = binder.pauseTask("anyTask");

        // Then
        assertThat(result).isFalse(); // SimpleJob doesn't support pause
    }

    @Test
    void testResumeTask() {
        // When
        boolean result = binder.resumeTask("anyTask");

        // Then
        assertThat(result).isFalse(); // SimpleJob doesn't support resume
    }

    @Test
    void testTriggerTask() {
        // When
        boolean result = binder.triggerTask("anyTask");

        // Then
        assertThat(result).isFalse(); // SimpleJob doesn't support manual trigger
    }

    @Test
    void testUpdateTaskCron() {
        // When
        boolean result = binder.updateTaskCron("anyTask", "0 0 12 * * ?");

        // Then
        assertThat(result).isFalse(); // SimpleJob doesn't support cron update
    }

    @Test
    void testTaskExists() throws Exception {
        // Given
        Method method = TestTaskExecutor.class.getMethod("execute");
        SchedulerTask task = SchedulerTask.builder()
            .taskName("existTask")
            .cron("0 0 12 * * ?")
            .method(method)
            .targetBean(testExecutor)
            .build();

        // When
        boolean beforeRegister = binder.taskExists("existTask");
        binder.registerTask(task);
        boolean afterRegister = binder.taskExists("existTask");

        // Then
        assertThat(beforeRegister).isFalse();
        assertThat(afterRegister).isTrue();
    }

    @Test
    void testRegisterTask_WithException() throws Exception {
        // Given
        Method method = FailingTaskExecutor.class.getMethod("failingMethod");
        SchedulerTask task = SchedulerTask.builder()
            .taskName("failingTask")
            .cron("*/1 * * * * ?")
            .method(method)
            .targetBean(new FailingTaskExecutor())
            .build();

        // When
        boolean result = binder.registerTask(task);

        // Then
        assertThat(result).isTrue();

        // Task should still be registered even if it throws exception
        assertThat(binder.taskExists("failingTask")).isTrue();
    }

    @Test
    void testInit() {
        // When - already called in setUp
        binder.init();

        // Then - should not throw exception
    }

    @Test
    void testDestroy() {
        // When
        binder.destroy();

        // Then - should not throw exception
    }

    // Test helper classes
    public static class TestTaskExecutor {
        private int executionCount = 0;

        public void execute() {
            executionCount++;
        }

        public int getExecutionCount() {
            return executionCount;
        }
    }

    public static class FailingTaskExecutor {
        public void failingMethod() {
            throw new RuntimeException("Task execution failed");
        }
    }
}

