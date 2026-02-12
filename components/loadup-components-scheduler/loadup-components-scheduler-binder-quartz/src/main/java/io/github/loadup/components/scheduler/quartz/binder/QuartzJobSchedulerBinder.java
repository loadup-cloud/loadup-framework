package io.github.loadup.components.scheduler.quartz.binder;

/*-
 * #%L
 * loadup-components-scheduler-binder-quartz
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

import io.github.loadup.components.scheduler.binder.AbstractSchedulerBinder;
import io.github.loadup.components.scheduler.binder.SchedulerBinder;
import io.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import io.github.loadup.components.scheduler.model.SchedulerTask;
import io.github.loadup.components.scheduler.quartz.cfg.QuartzBinderCfg;
import io.github.loadup.components.scheduler.quartz.job.SchedulerTaskJob;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

/** Quartz scheduler binder implementation. Supports distributed scheduling with Quartz features. */
@Slf4j
public class QuartzJobSchedulerBinder extends AbstractSchedulerBinder<QuartzBinderCfg, SchedulerBindingCfg>
        implements SchedulerBinder<QuartzBinderCfg, SchedulerBindingCfg> {

    private static final String BINDER_NAME = "quartz";
    private static final String DEFAULT_GROUP = "DEFAULT";
    // Store task name to group mapping
    private final Map<String, String> taskGroupMap = new ConcurrentHashMap<>();

    private Scheduler scheduler;

    @Override
    protected void onInit() {
        log.info("Initializing Quartz scheduler binder");
        try {
            Properties props = getBinderCfg().getQuartzProperties();

            // 1. 强制检查并设置最小线程数，防止因配置缺失导致崩溃
            if (!props.containsKey("org.quartz.threadPool.threadCount")) {
                // 默认取 10 或从配置中读取
                props.setProperty("org.quartz.threadPool.threadCount", "10");
            }

            // 2. 设置线程池类（这也是必须的）
            if (!props.containsKey("org.quartz.threadPool.class")) {
                props.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
            }

            // 3. 如果是分布式模式，必须配置 JobStore
            // props.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");

            SchedulerFactory schedulerFactory = new StdSchedulerFactory(props);
            this.scheduler = schedulerFactory.getScheduler();
            // 关键：如果需要 Job 访问 Spring Bean，需设置自定义 JobFactory
            //             this.scheduler.setJobFactory(springBeanJobFactory);
            this.scheduler.start();
        } catch (SchedulerException e) {
            throw new RuntimeException("Failed to init Quartz Scheduler", e);
        }
    }

    @Override
    public String getBinderType() {
        return QuartzJobSchedulerBinder.BINDER_NAME;
    }

    @Override
    public void binderDestroy() {
        log.info("Destroying Quartz scheduler binder");
        try {
            if (!scheduler.isShutdown()) {
                scheduler.shutdown(true);
                log.info("Quartz scheduler shutdown");
            }
        } catch (SchedulerException e) {
            log.error("Failed to shutdown Quartz scheduler", e);
        }
    }

    @Override
    public boolean schedule(SchedulerTask task) {
        try {
            String taskName = task.getTaskName();
            String taskGroup = task.getTaskGroup() != null ? task.getTaskGroup() : DEFAULT_GROUP;

            // Check if task already exists
            if (taskExists(taskName)) {
                log.warn("Task '{}' already exists, unregistering first", taskName);
                cancel(taskName);
            }

            // Create job detail
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("schedulerTask", task);

            JobDetail jobDetail = JobBuilder.newJob(SchedulerTaskJob.class)
                    .withIdentity(taskName, taskGroup)
                    .withDescription(task.getDescription())
                    .usingJobData(jobDataMap)
                    .storeDurably(false)
                    .build();

            // Create trigger
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(taskName + "_trigger", taskGroup)
                    .withSchedule(CronScheduleBuilder.cronSchedule(task.getCron()))
                    .build();

            // Schedule job
            scheduler.scheduleJob(jobDetail, trigger);

            // Store task group mapping
            taskGroupMap.put(taskName, taskGroup);

            log.info("Successfully registered task: {} with cron: {}", taskName, task.getCron());
            return true;

        } catch (Exception e) {
            log.error("Failed to register task: {}", task.getTaskName(), e);
            return false;
        }
    }

    @Override
    public boolean cancel(String taskName) {
        try {
            String taskGroup = taskGroupMap.getOrDefault(taskName, DEFAULT_GROUP);
            JobKey jobKey = new JobKey(taskName, taskGroup);
            boolean result = scheduler.deleteJob(jobKey);
            if (result) {
                taskGroupMap.remove(taskName);
                log.info("Successfully unregistered task: {}", taskName);
            } else {
                log.warn("Task '{}' not found for unregistration", taskName);
            }
            return result;
        } catch (SchedulerException e) {
            log.error("Failed to unregister task: {}", taskName, e);
            return false;
        }
    }

    @Override
    public boolean pauseTask(String taskName) {
        try {
            String taskGroup = taskGroupMap.getOrDefault(taskName, DEFAULT_GROUP);
            JobKey jobKey = new JobKey(taskName, taskGroup);
            scheduler.pauseJob(jobKey);
            log.info("Successfully paused task: {}", taskName);
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to pause task: {}", taskName, e);
            return false;
        }
    }

    @Override
    public boolean resumeTask(String taskName) {
        try {
            String taskGroup = taskGroupMap.getOrDefault(taskName, DEFAULT_GROUP);
            JobKey jobKey = new JobKey(taskName, taskGroup);
            scheduler.resumeJob(jobKey);
            log.info("Successfully resumed task: {}", taskName);
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to resume task: {}", taskName, e);
            return false;
        }
    }

    @Override
    public boolean triggerTask(String taskName) {
        try {
            String taskGroup = taskGroupMap.getOrDefault(taskName, DEFAULT_GROUP);
            JobKey jobKey = new JobKey(taskName, taskGroup);
            scheduler.triggerJob(jobKey);
            log.info("Successfully triggered task: {}", taskName);
            return true;
        } catch (SchedulerException e) {
            log.error("Failed to trigger task: {}", taskName, e);
            return false;
        }
    }

    @Override
    public boolean updateTaskCron(String taskName, String cron) {
        try {
            String taskGroup = taskGroupMap.getOrDefault(taskName, DEFAULT_GROUP);
            TriggerKey triggerKey = new TriggerKey(taskName + "_trigger", taskGroup);
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);

            if (trigger == null) {
                log.warn("Trigger not found for task: {}", taskName);
                return false;
            }

            CronTrigger newTrigger = trigger.getTriggerBuilder()
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build();

            scheduler.rescheduleJob(triggerKey, newTrigger);
            log.info("Successfully updated task '{}' cron to: {}", taskName, cron);
            return true;

        } catch (Exception e) {
            log.error("Failed to update task cron: {}", taskName, e);
            return false;
        }
    }

    @Override
    public boolean taskExists(String taskName) {
        try {
            String taskGroup = taskGroupMap.getOrDefault(taskName, DEFAULT_GROUP);
            JobKey jobKey = new JobKey(taskName, taskGroup);
            return scheduler.checkExists(jobKey);
        } catch (SchedulerException e) {
            log.error("Failed to check if task exists: {}", taskName, e);
            return false;
        }
    }
}
