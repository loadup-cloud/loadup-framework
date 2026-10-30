/*
 * #%L
 * LoadUp Modules Transfer Test
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
package io.github.loadup.modules.transfer.test;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.modules.transfer.domain.gateway.TransferGateway;
import io.github.loadup.modules.transfer.domain.model.TransferKind;
import io.github.loadup.modules.transfer.domain.model.TransferStatus;
import io.github.loadup.modules.transfer.domain.model.TransferTask;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TransferClaimIT.Application.class)
@EnableTestContainers(value = ContainerType.POSTGRESQL, reuse = false)
class TransferClaimIT {
    private final TransferGateway gateway;

    @Autowired
    TransferClaimIT(TransferGateway gateway) {
        this.gateway = gateway;
    }

    @Test
    void concurrentWorkersClaimOnceAndRunningTasksAreNotSelectedAgain() throws Exception {
        String id = UUID.randomUUID().toString();
        gateway.insert(
                new TransferTask(
                        id,
                        "owner",
                        TransferKind.EXPORT,
                        "test",
                        null,
                        null,
                        TransferStatus.QUEUED,
                        0,
                        0,
                        null,
                        LocalDateTime.now(),
                        null,
                        null),
                Map.of());
        assertThat(gateway.queued(100)).contains(id);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Callable<Boolean> claim = () -> {
                ready.countDown();
                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new AssertionError("Claim barrier timed out");
                }
                return gateway.start(id);
            };
            var first = executor.submit(claim);
            var second = executor.submit(claim);
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(true, false);
        } finally {
            start.countDown();
        }
        assertThat(gateway.queued(100)).doesNotContain(id);
        assertThat(gateway.start(id)).isFalse();
        gateway.fail(id, "Synthetic failure");
        assertThat(gateway.requeue(id)).isTrue();
        assertThat(gateway.start(id)).isTrue();
        gateway.succeed(id, null);
        assertThat(gateway.start(id)).isFalse();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {}
}
