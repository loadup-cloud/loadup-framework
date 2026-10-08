package io.github.loadup.retrytask.test.jobrunr;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.retrytask.facade.RetryTaskNotifier;
import io.github.loadup.retrytask.facade.model.RetryTaskFailure;
import io.github.loadup.retrytask.jobrunr.RetryTaskFailureNotifyingFilter;
import io.github.loadup.retrytask.jobrunr.RetryTaskJobRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jobrunr.jobs.Job;
import org.jobrunr.jobs.JobDetails;
import org.jobrunr.jobs.states.JobState;
import org.jobrunr.jobs.states.StateName;
import org.junit.jupiter.api.Test;

class RetryTaskFailureNotifyingFilterTest {

    @Test
    void dispatchesFinalFailureToNotifiers() {
        CapturingNotifier notifier = new CapturingNotifier();
        RetryTaskFailureNotifyingFilter filter = new RetryTaskFailureNotifyingFilter(List.of(notifier));

        Job job = newJob("order-sync", "order-1");
        JobState failedState = job.failed("permanent failure", new IllegalStateException("boom"))
                .getJobState();

        filter.onStateApplied(job, null, failedState);

        assertThat(notifier.failure).isNotNull();
        assertThat(notifier.failure.bizType()).isEqualTo("order-sync");
        assertThat(notifier.failure.bizId()).isEqualTo("order-1");
        assertThat(notifier.failure.attempts()).isEqualTo(1);
        assertThat(notifier.failure.errorMessage()).isEqualTo("permanent failure");
    }

    @Test
    void intermediateFailureIsNotReported() {
        CapturingNotifier notifier = new CapturingNotifier();
        RetryTaskFailureNotifyingFilter filter = new RetryTaskFailureNotifyingFilter(List.of(notifier));

        Job job = newJob("order-sync", "order-1");
        JobState failedState =
                job.failed("will retry", new IllegalStateException("boom")).getJobState();
        job.enqueue();

        filter.onStateApplied(job, null, failedState);

        assertThat(notifier.failure).isNull();
    }

    @Test
    void notifierFailureDoesNotBreakTheFilter() {
        RetryTaskNotifier throwing = failure -> {
            throw new IllegalStateException("notifier down");
        };
        RetryTaskFailureNotifyingFilter filter = new RetryTaskFailureNotifyingFilter(List.of(throwing));

        Job job = newJob("order-sync", "order-1");
        JobState failedState = job.failed("permanent failure", new IllegalStateException("boom"))
                .getJobState();

        filter.onStateApplied(job, null, failedState);

        assertThat(job.getState()).isEqualTo(StateName.FAILED);
    }

    private static Job newJob(String bizType, String bizId) {
        Job job = new Job(UUID.randomUUID(), new JobDetails(new RetryTaskJobRequest(bizType, bizId, Map.of(), 3)));
        job.setJobName(bizType + ":" + bizId);
        return job;
    }

    private static final class CapturingNotifier implements RetryTaskNotifier {
        private RetryTaskFailure failure;

        @Override
        public void notifyFailed(RetryTaskFailure failure) {
            this.failure = failure;
        }
    }
}
