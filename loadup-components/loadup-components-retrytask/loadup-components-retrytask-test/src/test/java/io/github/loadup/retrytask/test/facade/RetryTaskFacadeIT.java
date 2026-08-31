/*-
 * #%L
 * Loadup Components Retrytask Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

package io.github.loadup.retrytask.test.facade;

import static io.github.loadup.retrytask.facade.model.RetryTaskStatus.DELETED;
import static io.github.loadup.retrytask.facade.model.RetryTaskStatus.FAILED;
import static io.github.loadup.retrytask.facade.model.RetryTaskStatus.PENDING;
import static io.github.loadup.retrytask.facade.model.RetryTaskStatus.SUCCEEDED;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.model.RetryTaskRequest;
import io.github.loadup.retrytask.facade.model.RetryTaskStatus;
import io.github.loadup.retrytask.test.TestRetryTaskApplication;
import io.github.loadup.retrytask.test.TestRetryTaskProcessors;
import io.github.loadup.retrytask.test.TestRetryTaskProcessors.ControlledProcessor;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * End-to-end contract of the retry task facade on the JobRunr binder: registration, idempotency,
 * scheduling, retries, delete and reset, all backed by a real MySQL container.
 */
@SpringBootTest(classes = TestRetryTaskApplication.class)
@EnableTestContainers(ContainerType.MYSQL)
@ActiveProfiles("test")
class RetryTaskFacadeIT {

    private final String runId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

    @Autowired
    private RetryTaskFacade facade;

    @Autowired
    private TestRetryTaskProcessors processors;

    @Test
    void registerRunsImmediatelyAndSucceeds() {
        String bizType = TestRetryTaskProcessors.IMMEDIATE;
        String bizId = runId + "-order-1";
        ControlledProcessor processor = processors.processor(bizType);
        facade.register(RetryTaskRequest.of(bizType, bizId, Map.of("orderId", "1")));

        await().atMost(ofSeconds(15))
                .until(() -> facade.getStatus(bizType, bizId).orElse(null) == SUCCEEDED);
        assertThat(processor.calls()).isEqualTo(1);
        assertThat(processor.contexts())
                .anySatisfy(context -> assertThat(context.args()).containsEntry("orderId", "1"));
    }

    @Test
    void registerIsIdempotentWhileTaskIsActive() {
        String bizType = TestRetryTaskProcessors.BLOCKING;
        String bizId = runId + "-blocked-1";
        ControlledProcessor processor = processors.processor(bizType);
        UUID first = facade.register(RetryTaskRequest.of(bizType, bizId));

        await().atMost(ofSeconds(10)).until(() -> processor.calls() == 1);

        UUID second = facade.register(RetryTaskRequest.of(bizType, bizId));

        assertThat(second).isEqualTo(first);
        processor.releaseGate();
        await().atMost(ofSeconds(10))
                .until(() -> facade.getStatus(bizType, bizId).orElse(null) == SUCCEEDED);
        assertThat(processor.calls()).isEqualTo(1);
    }

    @Test
    void registerWithScheduleAtRunsLater() {
        String bizType = TestRetryTaskProcessors.SCHEDULED;
        String bizId = runId + "-later-1";
        ControlledProcessor processor = processors.processor(bizType);
        facade.register(RetryTaskRequest.schedule(bizType, bizId, Instant.now().plusSeconds(3)));

        assertThat(facade.getStatus(bizType, bizId)).contains(PENDING);

        await().atMost(ofSeconds(15))
                .until(() -> facade.getStatus(bizType, bizId).orElse(null) == SUCCEEDED);
        assertThat(processor.calls()).isEqualTo(1);
    }

    @Test
    void failedTaskIsRetriedUntilMaxRetriesExhausted() {
        String bizType = TestRetryTaskProcessors.FAILING;
        String bizId = runId + "-exhausted-1";
        ControlledProcessor processor = processors.processor(bizType);
        facade.register(RetryTaskRequest.of(bizType, bizId));

        await().atMost(ofSeconds(30))
                .until(() -> facade.getStatus(bizType, bizId).orElse(null) == FAILED);
        assertThat(processor.calls()).isEqualTo(2);
    }

    @Test
    void registerAfterFailureStartsFreshRun() {
        String bizType = TestRetryTaskProcessors.RETRY_AGAIN;
        String bizId = runId + "-retry-again-1";
        ControlledProcessor processor = processors.processor(bizType);
        facade.register(RetryTaskRequest.of(bizType, bizId));
        await().atMost(ofSeconds(30))
                .until(() -> facade.getStatus(bizType, bizId).orElse(null) == FAILED);

        processor.setFailOnCall(false);
        facade.register(RetryTaskRequest.of(bizType, bizId));

        await().atMost(ofSeconds(20))
                .until(() -> facade.getStatus(bizType, bizId).orElse(null) == SUCCEEDED);
        assertThat(processor.calls()).isEqualTo(3);
    }

    @Test
    void resetReenqueuesTaskWithOriginalPayload() {
        String bizType = TestRetryTaskProcessors.RESET;
        String bizId = runId + "-reset-1";
        ControlledProcessor processor = processors.processor(bizType);
        facade.register(RetryTaskRequest.of(bizType, bizId, Map.of("payloadKey", "payloadValue")));
        await().atMost(ofSeconds(30))
                .until(() -> facade.getStatus(bizType, bizId).orElse(null) == FAILED);

        processor.setFailOnCall(false);
        facade.reset(bizType, bizId);

        await().atMost(ofSeconds(20))
                .until(() -> facade.getStatus(bizType, bizId).orElse(null) == SUCCEEDED);
        assertThat(processor.calls()).isEqualTo(3);
        assertThat(processor.contexts())
                .anySatisfy(context -> assertThat(context.args()).containsEntry("payloadKey", "payloadValue"));
    }

    @Test
    void deleteStopsScheduledTask() {
        String bizType = TestRetryTaskProcessors.CANCELLED;
        String bizId = runId + "-cancelled-1";
        ControlledProcessor processor = processors.processor(bizType);
        facade.register(RetryTaskRequest.schedule(bizType, bizId, Instant.now().plusSeconds(4)));

        facade.delete(bizType, bizId);

        assertThat(facade.getStatus(bizType, bizId)).contains(DELETED);
        await().atMost(ofSeconds(7)).pollDelay(ofSeconds(6)).until(() -> processor.calls() == 0);
        assertThat(facade.getStatus(bizType, bizId)).contains(DELETED);
    }

    @Test
    void getStatusIsEmptyForUnknownTask() {
        Optional<RetryTaskStatus> status = facade.getStatus("never-registered", "nope");

        assertThat(status).isEmpty();
    }

    @Test
    void unknownBizTypeFailsWithoutRetry() {
        String bizId = runId + "-x-1";
        facade.register(new RetryTaskRequest("unknown-biz-type", bizId, null, null, 0));

        await().atMost(ofSeconds(15))
                .until(() -> facade.getStatus("unknown-biz-type", bizId).orElse(null) == FAILED);
    }
}
