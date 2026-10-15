/*
 * #%L
 * Loadup Scheduler Quartz Binder
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.scheduler.quartz;

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.scheduler.SchedulerTemplate;
import io.github.loadup.components.scheduler.model.ScheduleRequest;
import io.github.loadup.components.scheduler.model.SchedulerStatus;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.TimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

public class QuartzSchedulerTemplate implements SchedulerTemplate {

    private final Scheduler scheduler;

    public QuartzSchedulerTemplate(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void register(ScheduleRequest request) {
        try {
            JobKey jobKey = jobKey(request.taskName());
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
            JobDataMap data = new JobDataMap();
            data.put("taskName", request.taskName());
            data.put("args", new LinkedHashMap<>(request.args()));
            JobDetail detail = JobBuilder.newJob(SchedulerTaskJob.class)
                    .withIdentity(jobKey)
                    .usingJobData(data)
                    .storeDurably()
                    .build();
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey(request.taskName()))
                    .withSchedule(CronScheduleBuilder.cronSchedule(request.cron())
                            .inTimeZone(TimeZone.getTimeZone(resolveZoneId(request.zoneId()))))
                    .build();
            scheduler.scheduleJob(detail, trigger);
            LogUtil.debug(
                    QuartzSchedulerTemplate.class,
                    "Registered recurring task taskName={} cron={} zoneId={}",
                    request.taskName(),
                    request.cron(),
                    request.zoneId());
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to register recurring task '" + request.taskName() + "'", e);
        }
    }

    @Override
    public void delete(String taskName) {
        try {
            boolean removed = scheduler.deleteJob(jobKey(taskName));
            LogUtil.debug(
                    QuartzSchedulerTemplate.class, "Deleted recurring task taskName={} removed={}", taskName, removed);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to delete recurring task '" + taskName + "'", e);
        }
    }

    @Override
    public void trigger(String taskName) {
        try {
            if (!scheduler.checkExists(jobKey(taskName))) {
                LogUtil.debug(QuartzSchedulerTemplate.class, "No recurring task to trigger for taskName={}", taskName);
                return;
            }
            scheduler.triggerJob(jobKey(taskName));
            LogUtil.debug(QuartzSchedulerTemplate.class, "Triggered one run of recurring task taskName={}", taskName);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to trigger recurring task '" + taskName + "'", e);
        }
    }

    @Override
    public void updateCron(String taskName, String cron) {
        try {
            TriggerKey triggerKey = triggerKey(taskName);
            if (!scheduler.checkExists(jobKey(taskName))) {
                LogUtil.debug(QuartzSchedulerTemplate.class, "No recurring task to update for taskName={}", taskName);
                return;
            }
            var previous = scheduler.getTrigger(triggerKey);
            var schedule = CronScheduleBuilder.cronSchedule(cron);
            if (previous instanceof CronTrigger previousCron) {
                schedule.inTimeZone(previousCron.getTimeZone());
            }
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(schedule)
                    .build();
            scheduler.rescheduleJob(triggerKey, trigger);
            LogUtil.debug(
                    QuartzSchedulerTemplate.class,
                    "Updated cron of recurring task taskName={} cron={}",
                    taskName,
                    cron);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to update recurring task '" + taskName + "'", e);
        }
    }

    @Override
    public Optional<SchedulerStatus> getStatus(String taskName) {
        try {
            if (!scheduler.checkExists(jobKey(taskName))) {
                return Optional.empty();
            }
            TriggerState state = scheduler.getTriggerState(triggerKey(taskName));
            return Optional.of(state == TriggerState.PAUSED ? SchedulerStatus.PAUSED : SchedulerStatus.SCHEDULED);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to read recurring task '" + taskName + "'", e);
        }
    }

    private static JobKey jobKey(String taskName) {
        return JobKey.jobKey(taskName);
    }

    private static TriggerKey triggerKey(String taskName) {
        return TriggerKey.triggerKey(taskName);
    }

    private static ZoneId resolveZoneId(String zoneId) {
        return zoneId == null || zoneId.isBlank() ? ZoneId.systemDefault() : ZoneId.of(zoneId);
    }
}
