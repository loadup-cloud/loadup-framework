/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.loadup.components.scheduler.quartz.core;

import com.github.loadup.components.scheduler.model.TaskWrapper;
import com.github.loadup.components.scheduler.quartz.invoker.JobInvoker;
import com.github.loadup.components.scheduler.quartz.model.QuartzTaskWrapper;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author Lise
 */
@Slf4j
public abstract class AbstractScheduler {

    private final AtomicBoolean inited = new AtomicBoolean(false);

    protected String jobName;

    private Scheduler scheduler;

    private JobDetail jobDetail;

    private String instanceId;

    private String instancePath;

    private TaskWrapper taskWrapper;

    private String tenantId;

    public AbstractScheduler(String jobName, TaskWrapper taskWrapper, String instanceId, String tenantId) {
        this.jobName = jobName;
        this.instanceId = instanceId;
        this.taskWrapper = taskWrapper;
        this.tenantId = tenantId;
    }

    /**
     * new job
     */
    public abstract JobDetail newJob();

    /**
     * new invoker
     */
    public abstract JobInvoker newInvoker();

    public void init() {
        if (inited.compareAndSet(false, true)) {
            initScheduler();
        }
    }

    private void initScheduler() {
        try {

            QuartzTaskWrapper quartzTaskWrapper = new QuartzTaskWrapper();
            quartzTaskWrapper.setTaskWrapper(this.taskWrapper);
            quartzTaskWrapper.setJobName(this.jobName);
            quartzTaskWrapper.setJobInvoker(newInvoker());
            this.jobDetail = newJob();
            this.jobDetail.getJobDataMap().put("quartzTaskWrapper", quartzTaskWrapper);
            StdSchedulerFactory factory = new StdSchedulerFactory();
            factory.initialize(buildQuartzProperties());
            this.scheduler = factory.getScheduler();
            scheduleJob(this.taskWrapper.getCron());
        } catch (Exception e) {
            log.error("Init scheduler error. JobName: " + this.jobName, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Properties buildQuartzProperties() {
        Properties result = new Properties();
        result.put("org.quartz.threadPool.class", org.quartz.simpl.SimpleThreadPool.class.getName());
        result.put("org.quartz.threadPool.threadCount", "1");
        result.put("org.quartz.scheduler.instanceName", this.jobName);
        return result;
    }

    public synchronized void scheduleJob(String cron) {
        try {
            if (!scheduler.isStarted()) {
                if (!scheduler.checkExists(jobDetail.getKey())) {
                    scheduler.scheduleJob(jobDetail, createTrigger(cron));
                }
                scheduler.start();
                return;
            }

            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(TriggerKey.triggerKey(this.jobName));
            if (!scheduler.isShutdown() && null != trigger && !cron.equals(trigger.getCronExpression())) {
                scheduler.rescheduleJob(TriggerKey.triggerKey(this.jobName), createTrigger(cron));
            }
        } catch (Exception e) {
            log.error("Schedule job error. Job: " + this.jobName, e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private CronTrigger createTrigger(String cron) {
        return TriggerBuilder.newTrigger()
                .withIdentity(this.jobName)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionDoNothing())
                .build();
    }
}