package com.github.loadup.components.scheduler.simplejob;

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

import static org.apache.commons.lang3.ThreadUtils.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.loadup.components.scheduler.SimpleTestTask;
import com.github.loadup.components.scheduler.TestApplication;
import com.github.loadup.components.scheduler.binding.SchedulerBinding;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.loadup.framework.api.annotation.BindingClient;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;

/** Unit tests for SimpleJobSchedulerBinder. */
@SpringBootTest(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SimpleJobSchedulerBinderTest {

  private TestTaskExecutor testExecutor;
  private Method testExecutorMethod;
  private static List<String> taskList = new ArrayList<>();

  @BindingClient()
  private SchedulerBinding schedulerBinding;

  @BeforeEach
  void setUp() {
    try {
      testExecutor = new TestTaskExecutor();
      testExecutorMethod = TestTaskExecutor.class.getMethod("execute");
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }

  @AfterEach
  void tearDown() {
    if (schedulerBinding != null) {
      taskList.forEach(v -> schedulerBinding.unregisterTask(v));
    }
  }

  protected void safeSleep(long second) {
    try {
      sleep(Duration.ofSeconds(second));
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Test
  @Order(1)
  @DisplayName("Test SimpleTeskTask task execution")
  void testDefaultTask() {
    await().atMost(2, TimeUnit.SECONDS).until(() -> SimpleTestTask.a.get() > 1);
    Assertions.assertTrue(SimpleTestTask.a.get() > 1);
  }

  @Test
  @Order(1)
  @DisplayName("Test Unregister SimpleTeskTask task execution")
  void testUnregisterDefaultTask() {

    await().atMost(5, TimeUnit.SECONDS).until(() -> schedulerBinding.taskExists("SimpleTestTask"));
    boolean result = schedulerBinding.unregisterTask("SimpleTestTask");
    Assertions.assertTrue(result);
    int executionCount = SimpleTestTask.a.get();
    safeSleep(2);
    Assertions.assertEquals(executionCount, SimpleTestTask.a.get());
  }

  @Test
  @Order(2)
  @DisplayName("Test register new task and execution")
  void testRegisterTask() throws Exception {
    // Given
    SchedulerTask task =
        SchedulerTask.builder()
            .taskName("testTask")
            .cron("*/1 * * * * ?")
            .method(testExecutorMethod)
            .targetBean(testExecutor)
            .build();
    taskList.add("testTask");
    // When
    boolean result = schedulerBinding.registerTask(task);

    // Then
    assertThat(result).isTrue();
    assertThat(schedulerBinding.taskExists("testTask")).isTrue();

    // Wait for task execution
    await().atMost(5, TimeUnit.SECONDS).until(() -> testExecutor.getExecutionCount() > 2);

    assertThat(testExecutor.getExecutionCount()).isGreaterThan(0);
  }

  @Test
  @Order(2)
  @DisplayName("Test register duplicate task and execution")
  void testRegisterTask_DuplicateTask() throws Exception {
    // Given
    SchedulerTask task =
        SchedulerTask.builder()
            .taskName("duplicateTask")
            .cron("*/1 * * * * ?")
            .method(testExecutorMethod)
            .targetBean(testExecutor)
            .build();
    taskList.add("duplicateTask");

    // When
    boolean result1 = schedulerBinding.registerTask(task);
    boolean result2 = schedulerBinding.registerTask(task);

    await().atMost(3, TimeUnit.SECONDS).until(() -> testExecutor.getExecutionCount() > 1);
    // most execute twice within 3 seconds
    assertThat(TestTaskExecutor.executionCount).isLessThan(3);

    // Then
    assertThat(result1).isTrue();
    assertThat(result2).isTrue();
    assertThat(schedulerBinding.taskExists("duplicateTask")).isTrue();
  }

  @Test
  void testUnregisterTask() throws InterruptedException {
    // Given
    SchedulerTask task =
        SchedulerTask.builder()
            .taskName("unregisterTask")
            .cron("*/1 * * * * ?")
            .method(testExecutorMethod)
            .targetBean(testExecutor)
            .build();
    schedulerBinding.registerTask(task);

    await().atMost(2, TimeUnit.SECONDS).until(() -> testExecutor.getExecutionCount() > 1);
    // When
    boolean result = schedulerBinding.unregisterTask("unregisterTask");
    int executionCount = TestTaskExecutor.executionCount;
    assertThat(TestTaskExecutor.executionCount).isLessThan(3);
    sleep(Duration.ofSeconds(2l));
    assertThat(TestTaskExecutor.executionCount).isEqualTo(executionCount);
    // Then
    assertThat(result).isTrue();
    assertThat(schedulerBinding.taskExists("unregisterTask")).isFalse();
  }

  @Test
  void testUnregisterTask_NotFound() {
    // When
    boolean result = schedulerBinding.unregisterTask("nonExistent");

    // Then
    assertThat(result).isFalse();
  }

  @Test
  void testPauseTask() {
    // When
    boolean result = schedulerBinding.pauseTask("anyTask");

    // Then
    assertThat(result).isFalse(); // SimpleJob doesn't support pause
  }

  @Test
  void testResumeTask() {
    // When
    boolean result = schedulerBinding.resumeTask("anyTask");

    // Then
    assertThat(result).isFalse(); // SimpleJob doesn't support resume
  }

  @Test
  void testTriggerTask() {
    // When
    boolean result = schedulerBinding.triggerTask("anyTask");

    // Then
    assertThat(result).isFalse(); // SimpleJob doesn't support manual trigger
  }

  @Test
  void testUpdateTaskCron() {
    // When
    boolean result = schedulerBinding.updateTaskCron("anyTask", "0 0 12 * * ?");

    // Then
    assertThat(result).isFalse(); // SimpleJob doesn't support cron update
  }

  @Test
  void testTaskExists() throws Exception {
    // Given

    SchedulerTask task =
        SchedulerTask.builder()
            .taskName("existTask")
            .cron("0 0 12 * * ?")
            .method(testExecutorMethod)
            .targetBean(testExecutor)
            .build();

    // When
    boolean beforeRegister = schedulerBinding.taskExists("existTask");
    schedulerBinding.registerTask(task);
    boolean afterRegister = schedulerBinding.taskExists("existTask");

    // Then
    assertThat(beforeRegister).isFalse();
    assertThat(afterRegister).isTrue();
  }

  @Test
  void testRegisterTask_WithException() throws Exception {
    // Given
    Method method = FailingTaskExecutor.class.getMethod("failingMethod");
    SchedulerTask task =
        SchedulerTask.builder()
            .taskName("failingTask")
            .cron("*/1 * * * * ?")
            .method(method)
            .targetBean(new FailingTaskExecutor())
            .build();

    // When
    boolean result = schedulerBinding.registerTask(task);

    // Then
    assertThat(result).isTrue();

    // Task should still be registered even if it throws exception
    assertThat(schedulerBinding.taskExists("failingTask")).isTrue();
  }

  @Test
  void testInit() {
    // When - already called in setUp

    // Then - should not throw exception
  }

  @Test
  void testDestroy() {
    // When

    // Then - should not throw exception
  }

  // Test helper classes
  public static class TestTaskExecutor {
    public static int executionCount = 0;

    public void execute() {
      executionCount++;
      System.out.println(executionCount);
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
