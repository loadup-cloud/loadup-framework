package com.github.loadup.components.scheduler.quartz.listener;

import com.github.loadup.components.scheduler.annotation.DistributedScheduler;
import com.github.loadup.components.scheduler.core.TaskManager;
import com.github.loadup.components.scheduler.model.TaskWrapper;
import com.github.loadup.components.scheduler.quartz.core.SimpleScheduler;

import java.util.Map;
import java.util.Properties;
import javax.annotation.Resource;

import org.quartz.SchedulerException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class QuartzListener implements ApplicationListener<ApplicationStartedEvent> {
    @Resource
    private TaskManager taskManager;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        startAllTasks();
    }

    private void startAllTasks() {
        Map<String, TaskWrapper> taskMap = taskManager.findAllTask();
        taskMap.forEach((taskName, wrapper) -> {
            try {
                createScheduler(taskName, wrapper);
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void createScheduler(String taskName, TaskWrapper taskWrapper) throws SchedulerException {
        Class annotation = taskWrapper.getAnnotation();
        if (annotation == DistributedScheduler.class) {
            SimpleScheduler quartzScheduler = new SimpleScheduler(taskName, taskWrapper, "", "");
            quartzScheduler.init();
        }
    }

    private Properties buildQuartzProperties(String taskName) {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", org.quartz.simpl.SimpleThreadPool.class.getName());
        result.put("org.quartz.threadPool.threadCount", "1");
        result.put("org.quartz.scheduler.instanceName", taskName);
        return result;
    }
}
