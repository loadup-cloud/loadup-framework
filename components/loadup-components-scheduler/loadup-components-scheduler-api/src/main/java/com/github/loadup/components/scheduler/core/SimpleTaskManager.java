package com.github.loadup.components.scheduler.core;

import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import com.github.loadup.components.scheduler.model.TaskWrapper;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimpleTaskManager implements BeanPostProcessor, TaskManager {
    private static final Map<String, TaskWrapper> TASK_POOL = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            DistributedScheduler distributedScheduler =
                    AnnotationUtils.getAnnotation(method, DistributedScheduler.class);
            if (distributedScheduler != null) {
                String taskName = distributedScheduler.name();
                String cron = distributedScheduler.cron();
                TaskWrapper taskWrapper = new TaskWrapper();
                taskWrapper.setMethod(method);
                taskWrapper.setObject(bean);
                taskWrapper.setAnnotation(DistributedScheduler.class);
                taskWrapper.setCron(cron);
                saveTask(taskName, taskWrapper);
            }
        }
        return bean;
    }

    @Override
    public TaskWrapper findByTaskName(String taskName) {
        return TASK_POOL.get(taskName);
    }

    @Override
    public Map<String, TaskWrapper> findAllTask() {
        return TASK_POOL;
    }

    private void saveTask(String taskName, TaskWrapper taskWrapper) {
        if (TASK_POOL.containsKey(taskName)) {
            log.warn("task name duplicated! {} ", taskName);
            return;
        }
        TASK_POOL.put(taskName, taskWrapper);
    }
}
