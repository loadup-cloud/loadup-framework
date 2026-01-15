package com.github.loadup.components.scheduler.core;

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
import static org.mockito.Mockito.*;

import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import com.github.loadup.components.scheduler.api.SchedulerBinding;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 并发测试 - SchedulerTaskRegistry
 */
@ExtendWith(MockitoExtension.class)
class SchedulerTaskRegistryConcurrencyTest {

    @Mock
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
    }

    @Test
    @Timeout(10)
    void testConcurrentBeanRegistration() throws Exception {
        // Given
        int threadCount = 50;
        int beansPerThread = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        // When - 并发注册Bean
        for (int t = 0; t < threadCount; t++) {
            final int threadIndex = t;
            Future<?> future = executor.submit(() -> {
                try {
                    for (int i = 0; i < beansPerThread; i++) {
                        TestBeanConcurrent bean = new TestBeanConcurrent(threadIndex, i);
                        registry.postProcessAfterInitialization(bean,
                            "bean_" + threadIndex + "_" + i);
                    }
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        // Wait for all threads to complete
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Then - 验证所有任务都已注册
        int expectedTasks = threadCount * beansPerThread;
        assertThat(registry.getAllTasks()).hasSize(expectedTasks);
    }

    @Test
    @Timeout(10)
    void testConcurrentContextRefresh() throws Exception {
        // Given
        when(schedulerBinding.registerTask(any(SchedulerTask.class))).thenReturn(true);

        // 先注册一些任务
        for (int i = 0; i < 100; i++) {
            TestBeanConcurrent bean = new TestBeanConcurrent(0, i);
            registry.postProcessAfterInitialization(bean, "bean_" + i);
        }

        // When - 并发触发上下文刷新
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    registry.onApplicationEvent(mock(ContextRefreshedEvent.class));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Then - 验证所有任务都已注册到scheduler（并发场景下可能有重复调用）
        // 至少调用100次（每个任务至少一次），但由于并发可能会有重复
        verify(schedulerBinding, atLeast(100)).registerTask(any(SchedulerTask.class));
        // 验证注册的任务数量正确
        assertThat(registry.getAllTasks()).hasSize(100);
    }

    @Test
    @Timeout(10)
    void testConcurrentReadWrite() throws Exception {
        // Given
        int readerCount = 20;
        int writerCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(readerCount + writerCount);
        CountDownLatch latch = new CountDownLatch(readerCount + writerCount);
        AtomicInteger readSuccess = new AtomicInteger(0);
        AtomicInteger writeSuccess = new AtomicInteger(0);

        // When - 并发读写
        // Writers
        for (int i = 0; i < writerCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    TestBeanConcurrent bean = new TestBeanConcurrent(0, index);
                    registry.postProcessAfterInitialization(bean, "writer_bean_" + index);
                    writeSuccess.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Readers
        for (int i = 0; i < readerCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    Thread.sleep(10); // 稍微延迟以确保有数据可读
                    registry.getAllTasks();
                    readSuccess.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Then
        assertThat(writeSuccess.get()).isEqualTo(writerCount);
        assertThat(readSuccess.get()).isEqualTo(readerCount);
    }

    @Test
    @Timeout(10)
    void testConcurrentDuplicateTaskNames() throws Exception {
        // Given
        int threadCount = 50;
        String sharedTaskName = "sharedTask";
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When - 多个线程注册同名任务（但使用不同的beanName）
        for (int t = 0; t < threadCount; t++) {
            final int threadIndex = t;
            executor.submit(() -> {
                try {
                    SharedNameBean bean = new SharedNameBean(sharedTaskName);
                    // 使用不同的beanName，这样每个任务都会有唯一的名称
                    registry.postProcessAfterInitialization(bean, "sharedBean_" + threadIndex);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Then - 应该注册了threadCount个任务（因为每个都有不同的beanName）
        assertThat(registry.getAllTasks().size()).isEqualTo(threadCount);
    }

    @Test
    @Timeout(10)
    void testConcurrentClearAndRegister() throws Exception {
        // Given
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount * 2);
        AtomicInteger operationCount = new AtomicInteger(0);

        // When - 并发清空和注册
        for (int t = 0; t < threadCount; t++) {
            final int threadIndex = t;

            // Register task
            executor.submit(() -> {
                try {
                    TestBeanConcurrent bean = new TestBeanConcurrent(0, threadIndex);
                    registry.postProcessAfterInitialization(bean, "bean_" + threadIndex);
                    operationCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });

            // Clear operation (by registering and triggering context refresh)
            executor.submit(() -> {
                try {
                    Thread.sleep(5); // 稍微延迟
                    registry.onApplicationEvent(mock(ContextRefreshedEvent.class));
                    operationCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Then - 操作应该都完成
        assertThat(operationCount.get()).isEqualTo(threadCount * 2);
    }

    @Test
    @Timeout(10)
    void testHighConcurrencyStressTest() throws Exception {
        // Given
        int threadCount = 100;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger totalOperations = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        // When - 高并发压力测试
        for (int t = 0; t < threadCount; t++) {
            final int threadIndex = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < operationsPerThread; i++) {
                        try {
                            TestBeanConcurrent bean = new TestBeanConcurrent(threadIndex, i);
                            registry.postProcessAfterInitialization(bean,
                                "stress_bean_" + threadIndex + "_" + i);
                            totalOperations.incrementAndGet();
                        } catch (Exception e) {
                            errors.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Then
        int expectedOperations = threadCount * operationsPerThread;
        assertThat(totalOperations.get()).isEqualTo(expectedOperations);
        assertThat(errors.get()).isEqualTo(0);
    }

    // Test beans
    public static class TestBeanConcurrent {
        private final int threadId;
        private final int index;

        public TestBeanConcurrent(int threadId, int index) {
            this.threadId = threadId;
            this.index = index;
        }

        @DistributedScheduler(name = "", cron = "0 0 12 * * ?")
        public void scheduledMethod() {
        }

        public String getTaskName() {
            return "task_" + threadId + "_" + index;
        }
    }

    public static class SharedNameBean {
        private final String taskName;

        public SharedNameBean(String taskName) {
            this.taskName = taskName;
        }

        @DistributedScheduler(name = "", cron = "0 0 12 * * ?")
        public void scheduledMethod() {
        }
    }
}

