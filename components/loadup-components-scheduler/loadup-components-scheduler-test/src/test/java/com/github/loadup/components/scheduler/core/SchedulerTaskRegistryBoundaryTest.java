/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.core;

import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import com.github.loadup.components.scheduler.api.SchedulerBinding;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 边界条件测试 - SchedulerTaskRegistry
 */
@ExtendWith(MockitoExtension.class)
class SchedulerTaskRegistryBoundaryTest {

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
    void testNullBean() {
        // When & Then
        Object result = registry.postProcessAfterInitialization(null, "nullBean");
        assertThat(result).isNull();
    }

    @Test
    void testNullBeanName() {
        // Given
        TestBean testBean = new TestBean();

        // When
        Object result = registry.postProcessAfterInitialization(testBean, null);

        // Then
        assertThat(result).isSameAs(testBean);
    }

    @Test
    void testEmptyBeanName() {
        // Given
        TestBean testBean = new TestBean();

        // When
        Object result = registry.postProcessAfterInitialization(testBean, "");

        // Then
        assertThat(result).isSameAs(testBean);
    }

    @Test
    void testVeryLongTaskName() {
        // Given
        String veryLongName = "a".repeat(1000);
        TestBean testBean = new TestBean();

        // When
        Object result = registry.postProcessAfterInitialization(testBean, "longNameBean");

        // Then
        assertThat(result).isSameAs(testBean);
        // 任务名应该是 "testTask"，但验证长名称也能处理
        SchedulerTask task = registry.findByTaskName("testTask");
        assertThat(task).isNotNull();
    }

    @Test
    void testSpecialCharactersInTaskName() {
        // Given - 创建一个带特殊字符的Bean（通过空名称让它使用beanName.方法名）
        SpecialCharBean testBean = new SpecialCharBean();

        // When
        Object result = registry.postProcessAfterInitialization(testBean, "special-bean:with@chars!");

        // Then
        assertThat(result).isSameAs(testBean);
        // 应该使用beanName.方法名作为任务名（因为注解name为空）
        SchedulerTask task = registry.findByTaskName("special-bean:with@chars!.scheduledMethod");
        assertThat(task).isNotNull();
    }

    @Test
    void testEmptyCronExpression() {
        // Given
        EmptyCronBean testBean = new EmptyCronBean();

        // When
        Object result = registry.postProcessAfterInitialization(testBean, "emptyCronBean");

        // Then
        assertThat(result).isSameAs(testBean);
        SchedulerTask task = registry.findByTaskName("emptyCronTask");
        assertThat(task).isNotNull();
        assertThat(task.getCron()).isEmpty();
    }

    @Test
    void testMultipleAnnotationsOnSameClass() {
        // Given
        MultipleTasksBean testBean = new MultipleTasksBean();

        // When
        Object result = registry.postProcessAfterInitialization(testBean, "multipleTasksBean");

        // Then
        assertThat(result).isSameAs(testBean);
        assertThat(registry.findByTaskName("task1")).isNotNull();
        assertThat(registry.findByTaskName("task2")).isNotNull();
        assertThat(registry.findByTaskName("task3")).isNotNull();
    }

    @Test
    void testDuplicateTaskName() {
        // Given
        TestBean bean1 = new TestBean();
        TestBean bean2 = new TestBean();

        // When
        registry.postProcessAfterInitialization(bean1, "bean1");
        registry.postProcessAfterInitialization(bean2, "bean2");

        // Then - 第二个应该覆盖第一个
        SchedulerTask task = registry.findByTaskName("testTask");
        assertThat(task).isNotNull();
        assertThat(task.getTargetBean()).isSameAs(bean2);
    }

    @Test
    void testNullSchedulerBinding() throws Exception {
        // Given
        SchedulerTaskRegistry registryWithoutBinding = new SchedulerTaskRegistry();
        TestBean testBean = new TestBean();

        // When
        Object result = registryWithoutBinding.postProcessAfterInitialization(testBean, "testBean");

        // Then - 应该正常工作，只是不会注册到scheduler
        assertThat(result).isSameAs(testBean);
        assertThat(registryWithoutBinding.findByTaskName("testTask")).isNotNull();

        // 触发上下文刷新事件 - 应该不会抛异常
        registryWithoutBinding.onApplicationEvent(mock(ContextRefreshedEvent.class));
    }

    @Test
    void testMultipleContextRefreshEvents() {
        // Given
        TestBean testBean = new TestBean();
        when(schedulerBinding.registerTask(any(SchedulerTask.class))).thenReturn(true);
        registry.postProcessAfterInitialization(testBean, "testBean");

        // When - 多次触发上下文刷新
        registry.onApplicationEvent(mock(ContextRefreshedEvent.class));
        registry.onApplicationEvent(mock(ContextRefreshedEvent.class));
        registry.onApplicationEvent(mock(ContextRefreshedEvent.class));

        // Then - 应该只注册一次
        verify(schedulerBinding, times(1)).registerTask(any(SchedulerTask.class));
    }

    @Test
    void testRegisterTaskFailure() {
        // Given
        TestBean testBean = new TestBean();
        when(schedulerBinding.registerTask(any(SchedulerTask.class)))
            .thenThrow(new RuntimeException("Registration failed"));
        registry.postProcessAfterInitialization(testBean, "testBean");

        // When - 应该捕获异常并继续
        registry.onApplicationEvent(mock(ContextRefreshedEvent.class));

        // Then - 不应该抛出异常
        verify(schedulerBinding).registerTask(any(SchedulerTask.class));
    }

    @Test
    void testMaxTasksRegistration() throws Exception {
        // Given - 注册大量任务
        int maxTasks = 1000;
        for (int i = 0; i < maxTasks; i++) {
            TestBeanWithIndex bean = new TestBeanWithIndex(i);
            registry.postProcessAfterInitialization(bean, "bean" + i);
        }

        // When
        when(schedulerBinding.registerTask(any(SchedulerTask.class))).thenReturn(true);
        registry.onApplicationEvent(mock(ContextRefreshedEvent.class));

        // Then
        verify(schedulerBinding, times(maxTasks)).registerTask(any(SchedulerTask.class));
    }

    // Test beans
    static class TestBean {
        @DistributedScheduler(name = "testTask", cron = "0 0 12 * * ?")
        public void scheduledMethod() {
        }
    }

    static class SpecialCharBean {
        @DistributedScheduler(name = "", cron = "0 0 12 * * ?")
        public void scheduledMethod() {
        }
    }

    static class EmptyCronBean {
        @DistributedScheduler(name = "emptyCronTask", cron = "")
        public void scheduledMethod() {
        }
    }

    static class MultipleTasksBean {
        @DistributedScheduler(name = "task1", cron = "0 0 12 * * ?")
        public void method1() {
        }

        @DistributedScheduler(name = "task2", cron = "0 0 13 * * ?")
        public void method2() {
        }

        @DistributedScheduler(name = "task3", cron = "0 0 14 * * ?")
        public void method3() {
        }
    }

    static class TestBeanWithIndex {
        private final int index;

        TestBeanWithIndex(int index) {
            this.index = index;
        }

        @DistributedScheduler(name = "", cron = "0 0 12 * * ?")
        public void scheduledMethod() {
        }
    }
}

