package com.github.loadup.components.scheduler.performance;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import com.github.loadup.components.scheduler.api.SchedulerBinding;
import com.github.loadup.components.scheduler.core.SchedulerTaskRegistry;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 性能测试 - SchedulerTaskRegistry
 */
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SchedulerTaskRegistryPerformanceTest {

    @Mock(lenient = true)
    private SchedulerBinding schedulerBinding;

    private SchedulerTaskRegistry registry;

    @BeforeEach
    void setUp() throws Exception {
        registry = new SchedulerTaskRegistry();
        // 注入 schedulerBinding
        Field field = SchedulerTaskRegistry.class.getDeclaredField("schedulerBinding");
        field.setAccessible(true);
        field.set(registry, schedulerBinding);

        // 清空注册表
        Field registryField = SchedulerTaskRegistry.class.getDeclaredField("TASK_REGISTRY");
        registryField.setAccessible(true);
        Map<String, SchedulerTask> taskRegistry = (Map<String, SchedulerTask>) registryField.get(null);
        taskRegistry.clear();

        Field pendingField = SchedulerTaskRegistry.class.getDeclaredField("PENDING_TASKS");
        pendingField.setAccessible(true);
        Map<String, SchedulerTask> pendingTasks = (Map<String, SchedulerTask>) pendingField.get(null);
        pendingTasks.clear();

        when(schedulerBinding.registerTask(any(SchedulerTask.class))).thenReturn(true);
    }

    @Test
    @Order(1)
    @DisplayName("性能测试: 注册100个任务")
    void testRegister100Tasks() {
        int taskCount = 100;
        long startTime = System.nanoTime();

        // 注册任务
        for (int i = 0; i < taskCount; i++) {
            TestBean bean = new TestBean(i);
            registry.postProcessAfterInitialization(bean, "bean_" + i);
        }

        long duration = System.nanoTime() - startTime;
        double durationMs = duration / 1_000_000.0;

        System.out.printf("注册 %d 个任务耗时: %.2f ms (平均: %.4f ms/任务)%n",
            taskCount, durationMs, durationMs / taskCount);

        // 验证
        assertThat(registry.getAllTasks()).hasSize(taskCount);
        assertThat(durationMs).isLessThan(1000); // 应该在1秒内完成
    }

    @Test
    @Order(2)
    @DisplayName("性能测试: 注册1000个任务")
    void testRegister1000Tasks() {
        int taskCount = 1000;
        long startTime = System.nanoTime();

        // 注册任务
        for (int i = 0; i < taskCount; i++) {
            TestBean bean = new TestBean(i);
            registry.postProcessAfterInitialization(bean, "bean_" + i);
        }

        long duration = System.nanoTime() - startTime;
        double durationMs = duration / 1_000_000.0;

        System.out.printf("注册 %d 个任务耗时: %.2f ms (平均: %.4f ms/任务)%n",
            taskCount, durationMs, durationMs / taskCount);

        // 验证
        assertThat(registry.getAllTasks()).hasSize(taskCount);
        assertThat(durationMs).isLessThan(5000); // 应该在5秒内完成
    }

    @Test
    @Order(3)
    @DisplayName("性能测试: 注册10000个任务")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testRegister10000Tasks() {
        int taskCount = 10000;
        long startTime = System.nanoTime();

        // 注册任务
        for (int i = 0; i < taskCount; i++) {
            TestBean bean = new TestBean(i);
            registry.postProcessAfterInitialization(bean, "bean_" + i);
        }

        long duration = System.nanoTime() - startTime;
        double durationMs = duration / 1_000_000.0;

        System.out.printf("注册 %d 个任务耗时: %.2f ms (平均: %.4f ms/任务)%n",
            taskCount, durationMs, durationMs / taskCount);

        // 验证
        assertThat(registry.getAllTasks()).hasSize(taskCount);
        assertThat(durationMs).isLessThan(30000); // 应该在30秒内完成
    }

    @Test
    @Order(4)
    @DisplayName("性能测试: 查询任务性能")
    void testQueryPerformance() {
        // 先注册1000个任务
        int taskCount = 1000;
        for (int i = 0; i < taskCount; i++) {
            TestBean bean = new TestBean(i);
            registry.postProcessAfterInitialization(bean, "bean_" + i);
        }

        // 测试查询性能
        int queryCount = 10000;
        long startTime = System.nanoTime();

        for (int i = 0; i < queryCount; i++) {
            int taskIndex = i % taskCount;
            registry.findByTaskName("task_" + taskIndex);
        }

        long duration = System.nanoTime() - startTime;
        double durationMs = duration / 1_000_000.0;

        System.out.printf("执行 %d 次查询耗时: %.2f ms (平均: %.6f ms/查询)%n",
            queryCount, durationMs, durationMs / queryCount);

        assertThat(durationMs / queryCount).isLessThan(0.1); // 每次查询应该少于0.1ms
    }

    @Test
    @Order(5)
    @DisplayName("性能测试: 批量注册性能")
    void testBatchRegistrationPerformance() {
        int batchSize = 100;
        int batchCount = 10;
        List<Long> batchTimes = new ArrayList<>();

        for (int batch = 0; batch < batchCount; batch++) {
            long startTime = System.nanoTime();

            for (int i = 0; i < batchSize; i++) {
                int taskIndex = batch * batchSize + i;
                TestBean bean = new TestBean(taskIndex);
                registry.postProcessAfterInitialization(bean, "bean_" + taskIndex);
            }

            long duration = System.nanoTime() - startTime;
            batchTimes.add(duration / 1_000_000); // 转换为毫秒
        }

        // 计算统计信息
        double avgTime = batchTimes.stream().mapToLong(Long::longValue).average().orElse(0);
        long maxTime = batchTimes.stream().mapToLong(Long::longValue).max().orElse(0);
        long minTime = batchTimes.stream().mapToLong(Long::longValue).min().orElse(0);

        System.out.printf("批量注册性能 (每批 %d 个任务, 共 %d 批):%n", batchSize, batchCount);
        System.out.printf("  平均: %.2f ms%n", avgTime);
        System.out.printf("  最快: %d ms%n", minTime);
        System.out.printf("  最慢: %d ms%n", maxTime);

        // 验证
        assertThat(registry.getAllTasks()).hasSize(batchSize * batchCount);
        assertThat(avgTime).isLessThan(100); // 平均每批应该少于100ms
    }

    @Test
    @Order(6)
    @DisplayName("性能测试: 上下文刷新性能")
    void testContextRefreshPerformance() {
        // 先注册1000个任务
        int taskCount = 1000;
        for (int i = 0; i < taskCount; i++) {
            TestBean bean = new TestBean(i);
            registry.postProcessAfterInitialization(bean, "bean_" + i);
        }

        // 测试上下文刷新性能
        long startTime = System.nanoTime();
        registry.onApplicationEvent(mock(ContextRefreshedEvent.class));
        long duration = System.nanoTime() - startTime;
        double durationMs = duration / 1_000_000.0;

        System.out.printf("注册 %d 个任务到scheduler耗时: %.2f ms (平均: %.4f ms/任务)%n",
            taskCount, durationMs, durationMs / taskCount);

        assertThat(durationMs).isLessThan(5000); // 应该在5秒内完成
    }

    @Test
    @Order(7)
    @DisplayName("性能测试: 内存使用估算")
    void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        System.gc(); // 先触发GC

        long memBefore = runtime.totalMemory() - runtime.freeMemory();

        // 注册5000个任务
        int taskCount = 5000;
        for (int i = 0; i < taskCount; i++) {
            TestBean bean = new TestBean(i);
            registry.postProcessAfterInitialization(bean, "bean_" + i);
        }

        long memAfter = runtime.totalMemory() - runtime.freeMemory();
        long memUsed = memAfter - memBefore;
        double memUsedMB = memUsed / (1024.0 * 1024.0);
        double memPerTask = memUsed / (double) taskCount;

        System.out.printf("注册 %d 个任务内存使用:%n", taskCount);
        System.out.printf("  总内存: %.2f MB%n", memUsedMB);
        System.out.printf("  每任务: %.2f bytes%n", memPerTask);

        // 验证内存使用合理（每个任务应该少于10KB）
        assertThat(memPerTask).isLessThan(10240);
    }

    @Test
    @Order(8)
    @DisplayName("性能测试: 多任务Bean注册")
    void testMultipleTasksPerBean() {
        int beanCount = 100;
        long startTime = System.nanoTime();

        for (int i = 0; i < beanCount; i++) {
            MultipleTasksBean bean = new MultipleTasksBean(i);
            registry.postProcessAfterInitialization(bean, "multiBean_" + i);
        }

        long duration = System.nanoTime() - startTime;
        double durationMs = duration / 1_000_000.0;

        int totalTasks = beanCount * 3; // 每个Bean有3个任务
        System.out.printf("注册 %d 个Bean(每个3个任务，共 %d 个任务)耗时: %.2f ms%n",
            beanCount, totalTasks, durationMs);

        // 验证
        assertThat(registry.getAllTasks()).hasSize(totalTasks);
        assertThat(durationMs).isLessThan(1000);
    }

    @Test
    @Order(9)
    @DisplayName("性能测试: 完整流程压力测试")
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void testFullWorkflowStressTest() {
        int taskCount = 5000;

        // 1. 注册阶段
        long registerStart = System.nanoTime();
        for (int i = 0; i < taskCount; i++) {
            TestBean bean = new TestBean(i);
            registry.postProcessAfterInitialization(bean, "bean_" + i);
        }
        long registerDuration = (System.nanoTime() - registerStart) / 1_000_000;

        // 2. 查询阶段
        long queryStart = System.nanoTime();
        for (int i = 0; i < taskCount; i++) {
            registry.findByTaskName("task_" + i);
        }
        long queryDuration = (System.nanoTime() - queryStart) / 1_000_000;

        // 3. 获取所有任务
        long getAllStart = System.nanoTime();
        registry.getAllTasks();
        long getAllDuration = (System.nanoTime() - getAllStart) / 1_000_000;

        // 4. 触发上下文刷新
        long refreshStart = System.nanoTime();
        registry.onApplicationEvent(mock(ContextRefreshedEvent.class));
        long refreshDuration = (System.nanoTime() - refreshStart) / 1_000_000;

        // 输出结果
        System.out.printf("完整流程性能测试 (%d 个任务):%n", taskCount);
        System.out.printf("  1. 注册: %d ms (%.4f ms/任务)%n", registerDuration, registerDuration / (double) taskCount);
        System.out.printf("  2. 查询: %d ms (%.4f ms/查询)%n", queryDuration, queryDuration / (double) taskCount);
        System.out.printf("  3. 获取全部: %d ms%n", getAllDuration);
        System.out.printf("  4. 上下文刷新: %d ms%n", refreshDuration);
        System.out.printf("  总耗时: %d ms%n", registerDuration + queryDuration + getAllDuration + refreshDuration);

        // 验证
        assertThat(registry.getAllTasks()).hasSize(taskCount);
    }

    // Test beans
    public static class TestBean {
        private final int index;

        public TestBean(int index) {
            this.index = index;
        }

        @DistributedScheduler(name = "", cron = "0 0 12 * * ?")
        public void scheduledMethod() {
        }

        // 重写equals和hashCode以便正确标识不同的实例
        @Override
        public boolean equals(Object o) {
            if (this == o) {return true;}
            if (o == null || getClass() != o.getClass()) {return false;}
            TestBean testBean = (TestBean) o;
            return index == testBean.index;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(index);
        }
    }

    public static class MultipleTasksBean {
        private final int index;

        public MultipleTasksBean(int index) {
            this.index = index;
        }

        @DistributedScheduler(name = "", cron = "0 0 12 * * ?")
        public void task1() {
        }

        @DistributedScheduler(name = "", cron = "0 0 13 * * ?")
        public void task2() {
        }

        @DistributedScheduler(name = "", cron = "0 0 14 * * ?")
        public void task3() {
        }
    }
}

