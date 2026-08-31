package io.github.loadup.components.scheduler.jobrunr;

import io.github.loadup.components.scheduler.SchedulerTemplate;
import io.github.loadup.components.scheduler.model.ScheduleRequest;
import io.github.loadup.components.scheduler.model.SchedulerStatus;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobDetails;
import org.jobrunr.jobs.JobParameter;
import org.jobrunr.jobs.RecurringJob;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.scheduling.JobRequestScheduler;
import org.jobrunr.storage.Page;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.navigation.OffsetBasedPageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JobRunr backed {@link SchedulerTemplate}.
 *
 * <p>Recurring jobs are registered under their {@code taskName} as the recurring job id, which
 * makes registration idempotent: JobRunr updates an existing recurring job instead of creating a
 * duplicate. The recurring job runs through the same {@code BackgroundJobServer}, storage and
 * dashboard as the retry task component.
 */
public class JobRunrSchedulerTemplate implements SchedulerTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunrSchedulerTemplate.class);

    private final JobRequestScheduler scheduler;
    private final StorageProvider storageProvider;

    public JobRunrSchedulerTemplate(JobRequestScheduler scheduler, StorageProvider storageProvider) {
        this.scheduler = scheduler;
        this.storageProvider = storageProvider;
    }

    @Override
    public void register(ScheduleRequest request) {
        deletePendingRuns(request.taskName());
        SchedulerJobRequest payload = new SchedulerJobRequest(request.taskName(), request.args());
        scheduler.scheduleRecurrently(request.taskName(), request.cron(), resolveZoneId(request.zoneId()), payload);
        LOGGER.debug(
                "Registered recurring task taskName={} cron={} zoneId={}",
                request.taskName(),
                request.cron(),
                request.zoneId());
    }

    @Override
    public void delete(String taskName) {
        if (getStatus(taskName).isEmpty()) {
            LOGGER.debug("No recurring task to delete for taskName={}", taskName);
            return;
        }
        deletePendingRuns(taskName);
        scheduler.deleteRecurringJob(taskName);
        LOGGER.debug("Deleted recurring task taskName={}", taskName);
    }

    @Override
    public void trigger(String taskName) {
        SchedulerJobRequest payload = readPayload(taskName);
        if (payload == null) {
            LOGGER.debug("No recurring task to trigger for taskName={}", taskName);
            return;
        }
        scheduler.enqueue(payload);
        LOGGER.debug("Triggered one run of recurring task taskName={}", taskName);
    }

    @Override
    public void updateCron(String taskName, String cron) {
        Optional<RecurringJob> recurring = findRecurringJob(taskName);
        if (recurring.isEmpty()) {
            LOGGER.debug("No recurring task to update for taskName={}", taskName);
            return;
        }
        SchedulerJobRequest payload = readPayload(taskName);
        deletePendingRuns(taskName);
        scheduler.scheduleRecurrently(taskName, cron, ZoneId.of(recurring.get().getZoneId()), payload);
        LOGGER.debug("Updated cron of recurring task taskName={} cron={}", taskName, cron);
    }

    @Override
    public Optional<SchedulerStatus> getStatus(String taskName) {
        return findRecurringJob(taskName).map(recurring -> SchedulerStatus.SCHEDULED);
    }

    private Optional<RecurringJob> findRecurringJob(String taskName) {
        return storageProvider.getRecurringJobs().stream()
                .filter(recurring -> recurring.getId().equals(taskName))
                .findFirst();
    }

    private SchedulerJobRequest readPayload(String taskName) {
        Optional<RecurringJob> recurring = findRecurringJob(taskName);
        if (recurring.isEmpty()) {
            return null;
        }
        JobDetails jobDetails = recurring.get().getJobDetails();
        if (jobDetails != null && !jobDetails.getJobParameters().isEmpty()) {
            JobParameter parameter = jobDetails.getJobParameters().get(0);
            if (parameter.getObject() instanceof SchedulerJobRequest payload) {
                return payload;
            }
        }
        return new SchedulerJobRequest(taskName, Map.of());
    }

    private static ZoneId resolveZoneId(String zoneId) {
        return zoneId == null || zoneId.isBlank() ? ZoneId.systemDefault() : ZoneId.of(zoneId);
    }

    /**
     * Removes already scheduled one-time runs of a recurring job. JobRunr keeps these instances in
     * the job store; without removing them they would keep firing after the cron changed or the
     * recurring task was deleted, and they block new runs of the updated schedule.
     *
     * @param recurringJobId the recurring job id (the {@code taskName})
     */
    private void deletePendingRuns(String recurringJobId) {
        for (StateName state : List.of(StateName.AWAITING, StateName.SCHEDULED, StateName.ENQUEUED)) {
            long offset = 0;
            while (true) {
                Page<Job> page =
                        storageProvider.getJobs(state, new OffsetBasedPageRequest("updatedAt:ASC", offset, 1000));
                page.getItems().stream()
                        .filter(job -> job.getRecurringJobId()
                                .map(id -> id.equals(recurringJobId))
                                .orElse(false))
                        .forEach(job -> {
                            scheduler.delete(job.getId(), "Schedule changed or recurring task removed");
                            storageProvider.deletePermanently(job.getId());
                        });
                if (page.getItems().size() < 1000) {
                    break;
                }
                offset += 1000;
            }
        }
    }
}
