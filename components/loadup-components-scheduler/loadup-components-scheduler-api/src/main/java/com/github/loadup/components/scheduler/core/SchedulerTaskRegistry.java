/* Copyright (C) LoadUp Cloud 2022-2025 */
package com.github.loadup.components.scheduler.core;

/*-
 * #%L
 * loadup-components-scheduler-api
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import com.github.loadup.components.scheduler.api.SchedulerBinding;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing scheduled tasks.
 * Scans for @DistributedScheduler annotations and registers tasks.
 */
@Slf4j
public class SchedulerTaskRegistry implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    private static final Map<String, SchedulerTask> TASK_REGISTRY = new ConcurrentHashMap<String, SchedulerTask>();
    private static final Map<String, SchedulerTask> PENDING_TASKS = new ConcurrentHashMap<String, SchedulerTask>();

    @Autowired(required = false)
    private SchedulerBinding schedulerBinding;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            DistributedScheduler annotation =
                AnnotationUtils.getAnnotation(method, DistributedScheduler.class);
            if (annotation != null) {
                String taskName = annotation.name();
                if (taskName == null || taskName.trim().length() == 0) {
                    taskName = bean.getClass().getSimpleName() + "." + method.getName();
                }
                String cron = annotation.cron();

                SchedulerTask task = SchedulerTask.builder()
                    .taskName(taskName)
                    .cron(cron)
                    .method(method)
                    .targetBean(bean)
                    .annotation(DistributedScheduler.class)
                    .build();

                registerTask(task);

                // Store for later registration with scheduler binding
                PENDING_TASKS.put(taskName, task);
            }
        }
        return bean;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Register all pending tasks with scheduler binding after context is fully initialized
        if (schedulerBinding != null && !PENDING_TASKS.isEmpty()) {
            log.info("Context refreshed, registering {} pending tasks with scheduler", PENDING_TASKS.size());
            for (SchedulerTask task : PENDING_TASKS.values()) {
                try {
                    schedulerBinding.registerTask(task);
                    log.info("Registered task '{}' with scheduler", task.getTaskName());
                } catch (Exception e) {
                    log.error("Failed to register task '{}' with scheduler", task.getTaskName(), e);
                }
            }
            PENDING_TASKS.clear();
        }
    }

    /**
     * Register a task in the local registry.
     *
     * @param task the task to register
     */
    public void registerTask(SchedulerTask task) {
        String taskName = task.getTaskName();
        if (TASK_REGISTRY.containsKey(taskName)) {
            log.warn("Task name '{}' already exists, overwriting", taskName);
        }
        TASK_REGISTRY.put(taskName, task);
        log.debug("Task '{}' registered in local registry", taskName);
    }

    /**
     * Find a task by name.
     *
     * @param taskName the task name
     * @return the task or null if not found
     */
    public SchedulerTask findByTaskName(String taskName) {
        return TASK_REGISTRY.get(taskName);
    }

    /**
     * Get all registered tasks.
     *
     * @return collection of all tasks
     */
    public Collection<SchedulerTask> findAllTasks() {
        return TASK_REGISTRY.values();
    }

    /**
     * Get all registered tasks as a map.
     *
     * @return map of task name to task
     */
    public Map<String, SchedulerTask> getTaskRegistry() {
        return new ConcurrentHashMap<String, SchedulerTask>(TASK_REGISTRY);
    }

    /**
     * Remove a task from the registry.
     *
     * @param taskName the task name
     * @return the removed task or null if not found
     */
    public SchedulerTask removeTask(String taskName) {
        return TASK_REGISTRY.remove(taskName);
    }

    /**
     * Check if a task exists in the registry.
     *
     * @param taskName the task name
     * @return true if task exists
     */
    public boolean containsTask(String taskName) {
        return TASK_REGISTRY.containsKey(taskName);
    }

    /**
     * Get the count of registered tasks.
     *
     * @return the number of tasks
     */
    public int getTaskCount() {
        return TASK_REGISTRY.size();
    }
}

