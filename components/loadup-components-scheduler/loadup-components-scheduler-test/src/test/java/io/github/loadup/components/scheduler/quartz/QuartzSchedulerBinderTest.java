// package io.github.loadup.components.scheduler.quartz;
//
/// *-
// * #%L
// * loadup-components-scheduler-test
// * %%
// * Copyright (C) 2025 LoadUp Cloud
// * %%
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as
// * published by the Free Software Foundation, either version 3 of the
// * License, or (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public
// * License along with this program.  If not, see
// * <http://www.gnu.org/licenses/gpl-3.0.html>.
// * #L%
// */
//
// import static java.util.concurrent.TimeUnit.SECONDS;
// import static org.assertj.core.api.Assertions.assertThat;
// import static org.awaitility.Awaitility.await;
//
// import model.scheduler.components.loadup.github.io.SchedulerTask;
// import io.github.loadup.components.scheduler.quartz.binder.QuartzSchedulerBinder;
// import java.lang.reflect.Field;
// import java.lang.reflect.Method;
// import java.util.concurrent.atomic.AtomicInteger;
// import org.junit.jupiter.api.*;
// import org.quartz.Scheduler;
// import org.quartz.SchedulerException;
// import org.quartz.impl.StdSchedulerFactory;
//
/// **
// * Integration tests for QuartzSchedulerBinder.
// */
// class QuartzSchedulerBinderTest {
//
//    private QuartzSchedulerBinder binder;
//    private Scheduler             scheduler;
//    private TestTaskExecutor      testExecutor;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        // Create Quartz scheduler
//        StdSchedulerFactory factory = new StdSchedulerFactory();
//        scheduler = factory.getScheduler();
//
//        // Create binder and inject scheduler using reflection
//        binder = new QuartzSchedulerBinder();
//        Field schedulerField = QuartzSchedulerBinder.class.getDeclaredField("scheduler");
//        schedulerField.setAccessible(true);
//        schedulerField.set(binder, scheduler);
//
//        binder.init();
//
//        testExecutor = new TestTaskExecutor();
//    }
//
//    @AfterEach
//    void tearDown() throws SchedulerException {
//        if (binder != null) {
//            binder.destroy();
//        }
//    }
//
//    @Test
//    void testGetName() {
//        // When
//        String name = binder.getName();
//
//        // Then
//        assertThat(name).isEqualTo("quartz");
//    }
//
//    @Test
//    void testRegisterTask() throws Exception {
//        // Given
//        Method method = TestTaskExecutor.class.getMethod("execute");
//        SchedulerTask task = SchedulerTask.builder()
//            .taskName("testTask")
//            .cron("*/2 * * * * ?") // Every 2 seconds
//            .description("Test task")
//            .method(method)
//            .targetBean(testExecutor)
//            .build();
//
//        // When
//        boolean result = binder.registerTask(task);
//
//        // Then
//        assertThat(result).isTrue();
//        assertThat(binder.taskExists("testTask")).isTrue();
//
//        // Wait for task execution
//        await().atMost(5, SECONDS)
//            .until(() -> testExecutor.getExecutionCount() > 0);
//
//        assertThat(testExecutor.getExecutionCount()).isGreaterThan(0);
//    }
//
//    @Test
//    void testRegisterTask_DuplicateTask() throws Exception {
//        // Given
//        Method method = TestTaskExecutor.class.getMethod("execute");
//        SchedulerTask task = SchedulerTask.builder()
//            .taskName("duplicateTask")
//            .cron("0 0 12 * * ?")
//            .method(method)
//            .targetBean(testExecutor)
//            .build();
//
//        // When
//        boolean result1 = binder.registerTask(task);
//        boolean result2 = binder.registerTask(task);
//
//        // Then
//        assertThat(result1).isTrue();
//        assertThat(result2).isTrue();
//        assertThat(binder.taskExists("duplicateTask")).isTrue();
//    }
//
//    @Test
//    void testUnregisterTask() throws Exception {
//        // Given
//        Method method = TestTaskExecutor.class.getMethod("execute");
//        SchedulerTask task = SchedulerTask.builder()
//            .taskName("unregisterTask")
//            .cron("0 0 12 * * ?")
//            .method(method)
//            .targetBean(testExecutor)
//            .build();
//        binder.registerTask(task);
//
//        // When
//        boolean result = binder.unregisterTask("unregisterTask");
//
//        // Then
//        assertThat(result).isTrue();
//        assertThat(binder.taskExists("unregisterTask")).isFalse();
//    }
//
//    @Test
//    void testUnregisterTask_NotFound() {
//        // When
//        boolean result = binder.unregisterTask("nonExistent");
//
//        // Then
//        assertThat(result).isFalse();
//    }
//
//    @Test
//    void testPauseTask() throws Exception {
//        // Given
//        Method method = TestTaskExecutor.class.getMethod("execute");
//        SchedulerTask task = SchedulerTask.builder()
//            .taskName("pauseTask")
//            .cron("*/1 * * * * ?")
//            .method(method)
//            .targetBean(testExecutor)
//            .build();
//        binder.registerTask(task);
//
//        // When
//        boolean result = binder.pauseTask("pauseTask");
//
//        // Then
//        assertThat(result).isTrue();
//    }
//
//    @Test
//    void testResumeTask() throws Exception {
//        // Given
//        Method method = TestTaskExecutor.class.getMethod("execute");
//        SchedulerTask task = SchedulerTask.builder()
//            .taskName("resumeTask")
//            .cron("*/1 * * * * ?")
//            .method(method)
//            .targetBean(testExecutor)
//            .build();
//        binder.registerTask(task);
//        binder.pauseTask("resumeTask");
//
//        // When
//        boolean result = binder.resumeTask("resumeTask");
//
//        // Then
//        assertThat(result).isTrue();
//    }
//
//    @Test
//    void testTriggerTask() throws Exception {
//        // Given
//        Method method = TestTaskExecutor.class.getMethod("execute");
//        SchedulerTask task = SchedulerTask.builder()
//            .taskName("triggerTask")
//            .cron("0 0 12 * * ?") // Won't run naturally during test
//            .method(method)
//            .targetBean(testExecutor)
//            .build();
//        binder.registerTask(task);
//
//        // When
//        boolean result = binder.triggerTask("triggerTask");
//
//        // Then
//        assertThat(result).isTrue();
//
//        // Wait for manual trigger execution
//        await().atMost(3, SECONDS)
//            .until(() -> testExecutor.getExecutionCount() > 0);
//    }
//
//    @Test
//    void testUpdateTaskCron() throws Exception {
//        // Given
//        Method method = TestTaskExecutor.class.getMethod("execute");
//        SchedulerTask task = SchedulerTask.builder()
//            .taskName("updateTask")
//            .cron("0 0 12 * * ?")
//            .method(method)
//            .targetBean(testExecutor)
//            .build();
//        binder.registerTask(task);
//
//        // When
//        boolean result = binder.updateTaskCron("updateTask", "0 0 13 * * ?");
//
//        // Then
//        assertThat(result).isTrue();
//    }
//
//    @Test
//    void testUpdateTaskCron_NotFound() {
//        // When
//        boolean result = binder.updateTaskCron("nonExistent", "0 0 13 * * ?");
//
//        // Then
//        assertThat(result).isFalse();
//    }
//
//    @Test
//    void testTaskExists() throws Exception {
//        // Given
//        Method method = TestTaskExecutor.class.getMethod("execute");
//        SchedulerTask task = SchedulerTask.builder()
//            .taskName("existTask")
//            .cron("0 0 12 * * ?")
//            .method(method)
//            .targetBean(testExecutor)
//            .build();
//
//        // When
//        boolean beforeRegister = binder.taskExists("existTask");
//        binder.registerTask(task);
//        boolean afterRegister = binder.taskExists("existTask");
//
//        // Then
//        assertThat(beforeRegister).isFalse();
//        assertThat(afterRegister).isTrue();
//    }
//
//    @Test
//    void testRegisterTask_WithTaskGroup() throws Exception {
//        // Given
//        Method method = TestTaskExecutor.class.getMethod("execute");
//        SchedulerTask task = SchedulerTask.builder()
//            .taskName("groupTask")
//            .taskGroup("testGroup")
//            .cron("0 0 12 * * ?")
//            .method(method)
//            .targetBean(testExecutor)
//            .build();
//
//        // When
//        boolean result = binder.registerTask(task);
//
//        // Then
//        assertThat(result).isTrue();
//        assertThat(binder.taskExists("groupTask")).isTrue();
//    }
//
//    @Test
//    void testInit() throws SchedulerException {
//        // When - already called in setUp
//        binder.init();
//
//        // Then
//        assertThat(scheduler.isStarted()).isTrue();
//    }
//
//    @Test
//    void testDestroy() throws SchedulerException {
//        // When
//        binder.destroy();
//
//        // Then
//        assertThat(scheduler.isShutdown()).isTrue();
//    }
//
//    // Test helper class
//    public static class TestTaskExecutor {
//        private AtomicInteger executionCount = new AtomicInteger(0);
//
//        public void execute() {
//            executionCount.incrementAndGet();
//        }
//
//        public int getExecutionCount() {
//            return executionCount.get();
//        }
//    }
// }
//
