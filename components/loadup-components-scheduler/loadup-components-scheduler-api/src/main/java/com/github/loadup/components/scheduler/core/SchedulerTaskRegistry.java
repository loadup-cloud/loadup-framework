package com.github.loadup.components.scheduler.core;

/*-
 * #%L
 * loadup-components-scheduler-api
 * %%
 * Copyright (C) 2022 - 2023 loadup_cloud
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

import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import com.github.loadup.components.scheduler.api.SchedulerBinding;
import com.github.loadup.components.scheduler.model.SchedulerTask;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;

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
        // Handle null bean
        if (bean == null) {
            return null;
        }

        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            DistributedScheduler annotation =
                AnnotationUtils.getAnnotation(method, DistributedScheduler.class);
            if (annotation != null) {
                String taskName = annotation.name();
                if (taskName == null || taskName.trim().length() == 0) {
                    // Use beanName if available, otherwise use class name
                    String prefix = (beanName != null && !beanName.trim().isEmpty())
                        ? beanName
                        : bean.getClass().getSimpleName();
                    taskName = prefix + "." + method.getName();
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
     * Get all registered tasks.
     *
     * @return collection of all tasks
     */
    public java.util.Collection<SchedulerTask> getAllTasks() {
        return new java.util.ArrayList<>(TASK_REGISTRY.values());
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

