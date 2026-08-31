package io.github.loadup.components.scheduler.quartz;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quartz backed {@link SchedulerTemplate}.
 *
 * <p>Each recurring task is registered as a durable {@link JobDetail} plus a {@link CronTrigger},
 * both keyed by the {@code taskName}. Registration is idempotent: an existing job is replaced with
 * the new schedule and payload. Clustering is provided by Quartz itself via a JDBC job store.
 */
public class QuartzSchedulerTemplate implements SchedulerTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzSchedulerTemplate.class);

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
            LOGGER.debug(
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
            LOGGER.debug("Deleted recurring task taskName={} removed={}", taskName, removed);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to delete recurring task '" + taskName + "'", e);
        }
    }

    @Override
    public void trigger(String taskName) {
        try {
            if (!scheduler.checkExists(jobKey(taskName))) {
                LOGGER.debug("No recurring task to trigger for taskName={}", taskName);
                return;
            }
            scheduler.triggerJob(jobKey(taskName));
            LOGGER.debug("Triggered one run of recurring task taskName={}", taskName);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to trigger recurring task '" + taskName + "'", e);
        }
    }

    @Override
    public void updateCron(String taskName, String cron) {
        try {
            TriggerKey triggerKey = triggerKey(taskName);
            if (!scheduler.checkExists(jobKey(taskName))) {
                LOGGER.debug("No recurring task to update for taskName={}", taskName);
                return;
            }
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build();
            scheduler.rescheduleJob(triggerKey, trigger);
            LOGGER.debug("Updated cron of recurring task taskName={} cron={}", taskName, cron);
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
