package io.github.loadup.components.scheduler.test;

import static io.github.loadup.components.scheduler.model.SchedulerStatus.SCHEDULED;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.github.loadup.components.scheduler.SchedulerTemplate;
import io.github.loadup.components.scheduler.model.ScheduleRequest;
import io.github.loadup.components.scheduler.model.SchedulerStatus;
import io.github.loadup.components.scheduler.test.TestSchedulerProcessors.ControlledProcessor;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.util.Map;
import java.util.Optional;
import org.jobrunr.storage.StorageProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * End-to-end contract of the scheduler facade on the JobRunr binder: registration, idempotency,
 * cron updates, manual trigger and delete, all backed by a real MySQL container.
 */
@SpringBootTest(
        classes = TestSchedulerApplication.class,
        properties = "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration,"
                + "io.github.loadup.components.scheduler.quartz.autoconfig.QuartzSchedulerAutoConfiguration")
@EnableTestContainers(ContainerType.MYSQL)
@ActiveProfiles("test")
class SchedulerTemplateIT {

    @Autowired
    private SchedulerTemplate template;

    @Autowired
    private StorageProvider storageProvider;

    @Autowired
    private TestSchedulerProcessors processors;

    @Test
    void registerSchedulesRecurringRunWithPayload() {
        String taskName = TestSchedulerProcessors.IMMEDIATE;
        ControlledProcessor processor = processors.processor(taskName);
        template.register(ScheduleRequest.of(taskName, "*/5 * * * * *", Map.of("payloadKey", "payloadValue")));

        await().atMost(ofSeconds(20)).until(() -> processor.calls() >= 1);
        assertThat(processor.contexts())
                .anySatisfy(context -> assertThat(context.args()).containsEntry("payloadKey", "payloadValue"));
    }

    @Test
    void registerIsIdempotentForActiveSchedule() {
        String taskName = TestSchedulerProcessors.IDEMPOTENT;
        ControlledProcessor processor = processors.processor(taskName);
        template.register(ScheduleRequest.of(taskName, "*/5 * * * * *"));

        await().atMost(ofSeconds(10)).until(() -> processor.calls() == 1);

        template.register(ScheduleRequest.of(taskName, "*/5 * * * * *"));

        assertThat(recurringJobCount(taskName)).isEqualTo(1);
        assertThat(processor.calls()).isEqualTo(1);
        processor.releaseGate();
        await().atMost(ofSeconds(15)).until(() -> processor.calls() >= 2);
    }

    @Test
    void triggerRunsOneImmediateExecutionWithPayload() {
        String taskName = TestSchedulerProcessors.TRIGGER;
        ControlledProcessor processor = processors.processor(taskName);
        template.register(ScheduleRequest.of(taskName, "0 0 0 1 1 *", Map.of("triggerKey", "triggerValue")));

        template.trigger(taskName);

        await().atMost(ofSeconds(10)).until(() -> processor.calls() == 1);
        assertThat(processor.contexts().get(0).args()).containsEntry("triggerKey", "triggerValue");
    }

    @Test
    void updateCronChangesSchedule() {
        String taskName = TestSchedulerProcessors.CRON_UPDATE;
        ControlledProcessor processor = processors.processor(taskName);
        template.register(ScheduleRequest.of(taskName, "0 0 0 1 1 *"));

        template.updateCron(taskName, "*/5 * * * * *");

        await().atMost(ofSeconds(20)).until(() -> processor.calls() >= 1);
        assertThat(template.getStatus(taskName)).contains(SCHEDULED);
    }

    @Test
    void deleteRemovesRecurringTask() {
        String taskName = TestSchedulerProcessors.DELETE;
        template.register(ScheduleRequest.of(taskName, "0 0 0 1 1 *"));
        assertThat(template.getStatus(taskName)).contains(SCHEDULED);

        template.delete(taskName);

        assertThat(template.getStatus(taskName)).isEmpty();
        assertThat(recurringJobCount(taskName)).isZero();
    }

    @Test
    void getStatusIsEmptyForUnknownTask() {
        Optional<SchedulerStatus> status = template.getStatus("never-registered");

        assertThat(status).isEmpty();
    }

    private long recurringJobCount(String taskName) {
        return storageProvider.getRecurringJobs().stream()
                .filter(recurring -> recurring.getId().equals(taskName))
                .count();
    }
}
