package io.github.loadup.components.scheduler.test;

import static io.github.loadup.components.scheduler.model.SchedulerStatus.SCHEDULED;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.github.loadup.components.scheduler.SchedulerTemplate;
import io.github.loadup.components.scheduler.model.ScheduleRequest;
import io.github.loadup.components.scheduler.model.SchedulerStatus;
import io.github.loadup.components.scheduler.test.TestSchedulerProcessors.ControlledProcessor;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * End-to-end contract of the scheduler facade on the Quartz binder with the in-memory job store:
 * registration, cron updates, manual trigger and delete.
 */
@SpringBootTest(
        classes = TestSchedulerApplication.class,
        properties = "spring.autoconfigure.exclude="
                + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                + "org.jobrunr.spring.autoconfigure.JobRunrAutoConfiguration,"
                + "io.github.loadup.components.scheduler.jobrunr.autoconfig.SchedulerJobRunrAutoConfiguration")
@ActiveProfiles("test")
class QuartzSchedulerTemplateIT {

    @Autowired
    private SchedulerTemplate template;

    @Autowired
    private TestSchedulerProcessors processors;

    @Test
    void registerSchedulesRecurringRunWithPayload() {
        String taskName = TestSchedulerProcessors.IMMEDIATE;
        ControlledProcessor processor = processors.processor(taskName);
        template.register(ScheduleRequest.of(taskName, "*/2 * * * * ?", Map.of("payloadKey", "payloadValue")));

        await().atMost(ofSeconds(20)).until(() -> processor.calls() >= 1);
        assertThat(processor.contexts())
                .anySatisfy(context -> assertThat(context.args()).containsEntry("payloadKey", "payloadValue"));
    }

    @Test
    void triggerRunsOneImmediateExecutionWithPayload() {
        String taskName = TestSchedulerProcessors.TRIGGER;
        ControlledProcessor processor = processors.processor(taskName);
        template.register(ScheduleRequest.of(taskName, "0 0 0 1 1 ?", Map.of("triggerKey", "triggerValue")));

        template.trigger(taskName);

        await().atMost(ofSeconds(10)).until(() -> processor.calls() == 1);
        assertThat(processor.contexts().get(0).args()).containsEntry("triggerKey", "triggerValue");
    }

    @Test
    void updateCronChangesSchedule() {
        String taskName = TestSchedulerProcessors.CRON_UPDATE;
        ControlledProcessor processor = processors.processor(taskName);
        template.register(ScheduleRequest.of(taskName, "0 0 0 1 1 ?"));

        template.updateCron(taskName, "*/2 * * * * ?");

        await().atMost(ofSeconds(20)).until(() -> processor.calls() >= 1);
        assertThat(template.getStatus(taskName)).contains(SCHEDULED);
    }

    @Test
    void deleteRemovesRecurringTask() {
        String taskName = TestSchedulerProcessors.DELETE;
        template.register(ScheduleRequest.of(taskName, "0 0 0 1 1 ?"));
        assertThat(template.getStatus(taskName)).contains(SCHEDULED);

        template.delete(taskName);

        assertThat(template.getStatus(taskName)).isEmpty();
    }

    @Test
    void getStatusIsEmptyForUnknownTask() {
        Optional<SchedulerStatus> status = template.getStatus("never-registered");

        assertThat(status).isEmpty();
    }
}
