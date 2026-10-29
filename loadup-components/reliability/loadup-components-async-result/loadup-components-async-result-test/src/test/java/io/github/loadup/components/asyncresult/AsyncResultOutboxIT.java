/*
 * #%L
 * LoadUp Async Result Test
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
package io.github.loadup.components.asyncresult;

import static org.assertj.core.api.Assertions.*;

import io.github.loadup.components.asyncresult.local.LocalAsyncResultProvider;
import io.github.loadup.components.outbox.*;
import io.github.loadup.components.testcontainers.annotation.*;
import io.github.loadup.components.testcontainers.database.SharedPostgreSQLContainer;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = AsyncResultOutboxIT.Application.class)
@ActiveProfiles("test")
@EnableTestContainers(value = ContainerType.POSTGRESQL, reuse = false)
class AsyncResultOutboxIT {
    @SpringBootConfiguration
    static class Application {}

    @Test
    void outboxBecomesVisibleAfterCommitAndCompletesAnAlreadyRegisteredWaiter() {
        var ds = new DriverManagerDataSource(
                SharedPostgreSQLContainer.getJdbcUrl(),
                SharedPostgreSQLContainer.getUsername(),
                SharedPostgreSQLContainer.getPassword());
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V20261008000001__create_outbox.sql"))
                .execute(ds);
        var props = new OutboxProperties();
        var manager = new DataSourceTransactionManager(ds);
        var outbox = new JdbcOutboxRepository(ds, manager, props);
        var tx = new TransactionTemplate(manager);
        var mapper = JsonMapper.builder().build();
        try (var provider = new LocalAsyncResultProvider(100);
                var template = AsyncResultTest.template(provider, AsyncResultTest.settings(5, Duration.ofSeconds(3)))) {
            var eventId = new AtomicReference<String>();
            var handler = new OutboxHandler() {
                public String eventType() {
                    return "async-result.echo";
                }

                public void handle(OutboxEvent event) {
                    var key = mapper.readValue(event.message().payload(), AsyncReplyKey.class);
                    assertThat(template.complete(key, "committed")).isEqualTo(AsyncCompletionStatus.ACCEPTED);
                }
            };
            var dispatcher = new OutboxDispatcher(outbox, props, List.of(handler), null);
            String result = template.execute(AsyncResultTest.options(), String.class, key -> {
                tx.executeWithoutResult(status -> eventId.set(outbox.publish(new OutboxMessage(
                        key.tenantId(), handler.eventType(), 1, key.businessId(), mapper.writeValueAsString(key)))));
                assertThat(dispatcher.dispatchBatch()).isEqualTo(1);
                return AsyncResultTest.sent();
            });
            assertThat(result).isEqualTo("committed");
            assertThat(outbox.find("tenant", eventId.get())).isPresent();
            tx.executeWithoutResult(status -> assertThatThrownBy(
                            () -> template.execute(AsyncResultTest.options(), String.class, k -> {
                                throw new AssertionError("must not dispatch inside transaction");
                            }))
                    .isInstanceOf(io.github.loadup.commons.error.CommonException.class));
        }
    }
}
