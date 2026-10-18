/*
 * #%L
 * Loadup Components Scheduler Test
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
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.jobrunr.jobs.states.StateName;
import org.jobrunr.storage.StorageProvider;
import org.jobrunr.storage.navigation.OffsetBasedPageRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = TestSchedulerApplication.class,
        properties = "spring.autoconfigure.exclude="
                + "org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration,"
                + "io.github.loadup.components.scheduler.quartz.autoconfigure.QuartzSchedulerAutoConfiguration")
@EnableTestContainers(value = ContainerType.MYSQL, reuse = false)
@org.springframework.test.annotation.DirtiesContext(
        classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
class SchedulerTemplateIT {

    @Autowired
    private SchedulerTemplate template;

    @Autowired
    private StorageProvider storageProvider;

    @Autowired
    private TestSchedulerProcessors processors;

    @Test
    void loadsOnePatchedJobRunrCore() throws Exception {
        var resources = SchedulerTemplateIT.class
                .getClassLoader()
                .getResources("org/jobrunr/server/tasks/zookeeper/ProcessRecurringJobsTask.class");
        assertThat(Collections.list(resources)).hasSize(1);
        assertThat(org.jobrunr.server.tasks.zookeeper.ProcessRecurringJobsTask.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation())
                .isEqualTo(org.jobrunr.jobs.Job.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation())
                .isEqualTo(io.github.loadup.components.jobrunr.RecurringJobCacheInvalidator.class
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation());
    }

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

        // Ensure the old far-future run has been cached before updating the recurring schedule.
        await().atMost(ofSeconds(15))
                .until(
                        () -> storageProvider
                                .getJobs(StateName.SCHEDULED, new OffsetBasedPageRequest("updatedAt:ASC", 0L, 100))
                                .getItems()
                                .stream()
                                .anyMatch(job -> job.getRecurringJobId()
                                        .filter(taskName::equals)
                                        .isPresent()));
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
