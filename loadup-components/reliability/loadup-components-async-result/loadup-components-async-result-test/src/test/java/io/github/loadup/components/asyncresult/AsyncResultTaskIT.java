/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.asyncresult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.asyncresult.local.LocalAsyncResultProvider;
import io.github.loadup.components.task.JdbcTaskRepository;
import io.github.loadup.components.task.TaskDispatcher;
import io.github.loadup.components.task.TaskExecution;
import io.github.loadup.components.task.TaskHandler;
import io.github.loadup.components.task.TaskProperties;
import io.github.loadup.components.task.TaskStatus;
import io.github.loadup.components.task.TaskSubmission;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.components.testcontainers.database.SharedPostgreSQLContainer;
import io.micrometer.observation.ObservationRegistry;
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

@SpringBootTest(classes = AsyncResultTaskIT.Application.class)
@ActiveProfiles("test")
@EnableTestContainers(value = ContainerType.POSTGRESQL, reuse = false)
class AsyncResultTaskIT {
    @Test
    void taskBecomesVisibleAfterCommitAndCompletesAnAlreadyRegisteredWaiter() {
        var ds = new DriverManagerDataSource(
                SharedPostgreSQLContainer.getJdbcUrl(),
                SharedPostgreSQLContainer.getUsername(),
                SharedPostgreSQLContainer.getPassword());
        new ResourceDatabasePopulator(new ClassPathResource("db/schema/task.sql")).execute(ds);
        var props = new TaskProperties();
        var manager = new DataSourceTransactionManager(ds);
        var tasks = new JdbcTaskRepository(ds, manager, props);
        var tx = new TransactionTemplate(manager);
        var mapper = JsonMapper.builder().build();
        try (var provider = new LocalAsyncResultProvider(100);
                var template = AsyncResultTest.template(provider, AsyncResultTest.settings(5, Duration.ofSeconds(3)))) {
            var eventId = new AtomicReference<String>();
            var handler = new TaskHandler() {
                public String taskType() {
                    return "async-result.echo";
                }

                public void handle(TaskExecution event) {
                    var key = mapper.readValue(event.payload(), AsyncReplyKey.class);
                    assertThat(template.complete(key, "committed")).isEqualTo(AsyncCompletionStatus.ACCEPTED);
                }
            };
            var dispatcher = new TaskDispatcher(tasks, props, List.of(handler), null, ObservationRegistry.NOOP);
            String result = template.execute(AsyncResultTest.options(), String.class, key -> {
                tx.executeWithoutResult(status -> eventId.set(tasks.submit(TaskSubmission.immediate(
                        handler.taskType(), 1, key.businessId(), mapper.writeValueAsString(key)))));
                dispatcher.execute();
                return AsyncResultTest.sent();
            });
            assertThat(result).isEqualTo("committed");
            assertThat(tasks.find(eventId.get()))
                    .hasValueSatisfying(task -> assertThat(task.status()).isEqualTo(TaskStatus.SUCCEEDED));
            tx.executeWithoutResult(status -> assertThatThrownBy(
                            () -> template.execute(AsyncResultTest.options(), String.class, k -> {
                                throw new AssertionError("must not dispatch inside transaction");
                            }))
                    .isInstanceOf(CommonException.class));
        }
    }

    @SpringBootConfiguration
    static class Application {}
}
