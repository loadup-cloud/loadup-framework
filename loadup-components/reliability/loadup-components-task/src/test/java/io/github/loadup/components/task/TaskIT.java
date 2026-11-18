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
package io.github.loadup.components.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.http.HttpCall;
import io.github.loadup.components.http.HttpProperties;
import io.github.loadup.components.http.HttpTemplate;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = TaskIT.Application.class)
@ActiveProfiles("test")
@EnableTestContainers(value = ContainerType.POSTGRESQL, reuse = false)
class TaskIT {
    private final JdbcTaskRepository repository;
    private final TaskProperties properties;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    @Autowired
    TaskIT(
            JdbcTaskRepository repository,
            TaskProperties properties,
            DataSource source,
            PlatformTransactionManager manager) {
        this.repository = repository;
        this.properties = properties;
        jdbc = new JdbcTemplate(source);
        transactions = new TransactionTemplate(manager);
        ensureSchema(source, "loadup_task", "db/task/pg/schema.sql");
    }

    private static void ensureSchema(DataSource source, String table, String resource) {
        JdbcTemplate jdbc = new JdbcTemplate(source);
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables"
                        + " WHERE table_schema = current_schema() AND table_name = ?",
                Integer.class,
                table);
        if (count == null || count == 0) {
            new ResourceDatabasePopulator(new ClassPathResource(resource)).execute(source);
        }
    }

    @BeforeEach
    void reset() {
        jdbc.update("DELETE FROM loadup_task_operation");
        jdbc.update("DELETE FROM loadup_task");
    }

    @Test
    void publicationRequiresTransactionAndRollsBackWithBusinessWork() {
        assertThatThrownBy(() -> repository.submit(message())).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> transactions.execute(status -> {
                    repository.submit(message());
                    throw new IllegalStateException("rollback");
                }))
                .isInstanceOf(IllegalStateException.class);
        assertThat(repository.pendingCount()).isZero();
        String id = publish();
        assertThat(repository.find(id)).isPresent();
        var sameWork = repository.find(id).orElseThrow();
        String duplicate = transactions.execute(status ->
                repository.submit(TaskSubmission.immediate(sameWork.taskType(), sameWork.bizId(), sameWork.payload())));
        assertThat(duplicate).isEqualTo(id);
    }

    @Test
    void expiredClaimIsRecoveredAndOldOwnerCannotAcknowledge() {
        String id = publish();
        var first = repository.claim().orElseThrow();
        jdbc.update(
                "UPDATE loadup_task SET lease_until = (clock_timestamp() + INTERVAL '-1 seconds') WHERE id = ?", id);
        var recovered = repository.claim().orElseThrow();
        assertThat(recovered.event().id()).isEqualTo(id);
        assertThat(recovered.token()).isNotEqualTo(first.token());
        assertThat(repository.complete(first)).isFalse();
        assertThat(repository.fail(first, new IllegalStateException("stale"))).isFalse();
        assertThat(repository.complete(recovered)).isTrue();
        assertThat(repository.find(id)).isEmpty();
        assertThat(repository.fail(first, new IllegalStateException("late"))).isFalse();
    }

    @Test
    void exhaustedLeaseBecomesFailedAndReplayIsAudited() {
        String id = publish();
        repository.claim().orElseThrow();
        jdbc.update(
                "UPDATE loadup_task SET attempts = ?, lease_until = (clock_timestamp() + INTERVAL '-1 seconds') WHERE id = ?",
                properties.getMaxAttempts(),
                id);
        assertThat(repository.claim()).isEmpty();
        assertThat(repository.failures(10)).hasSize(1);
        repository.retryFailed(id, "operator", "reviewed");
        assertThat(repository.claim().orElseThrow().event().id()).isEqualTo(id);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM loadup_task_operation", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void concurrentWorkersDoNotShareAnActiveClaim() throws Exception {
        publish();
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> {
                return repository.claim();
            });
            var second = executor.submit(() -> {
                return repository.claim();
            });
            assertThat(List.of(first.get(), second.get()).stream()
                            .filter(Optional::isPresent)
                            .count())
                    .isEqualTo(1);
        }
    }

    @Test
    void committedEventDrivesHttpAndPreservesCallerContext() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        var accepted = ConcurrentHashMap.<String>newKeySet();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/notify", exchange -> {
            assertThat(exchange.getRequestHeaders().getFirst("Idempotency-Key")).isNotBlank();
            accepted.add(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
            calls.incrementAndGet();
            exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();
        var settings = new HttpProperties.Client(
                URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                null,
                null,
                Map.of("notify", new HttpProperties.Operation(HttpMethod.POST, "/notify", null)));
        try (HttpTemplate http = new HttpTemplate(
                new HttpProperties(Map.of("merchant", settings)),
                RestClient::builder,
                JsonMapper.builder().build(),
                null,
                null,
                null)) {
            String id = publish();
            TaskHandler handler = new TaskHandler() {
                public String taskType() {
                    return "test.created";
                }

                public void handle(TaskExecution event) {
                    var response = http.exchange(
                            "merchant",
                            "notify",
                            new HttpCall(
                                    null,
                                    null,
                                    Map.of("Idempotency-Key", event.id(), "Content-Type", "application/json"),
                                    event.payload()));
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new IllegalStateException("Notification rejected");
                    }
                    if (calls.get() == 1) {
                        throw new IllegalStateException("Simulated response loss after acceptance");
                    }
                }
            };
            var dispatcher = new TaskDispatcher(repository, properties, List.of(handler), null);
            dispatcher.execute();
            jdbc.update("UPDATE loadup_task SET next_execute_at = CURRENT_TIMESTAMP(6) WHERE id = ?", id);
            dispatcher.execute();
            dispatcher.execute();
            assertThat(calls).hasValue(2);
            assertThat(accepted).containsExactly(id);
            assertThat(repository.find(id)).isEmpty();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void readOnlyTransactionsAndMissingArgumentsCannotSubmit() {
        transactions.setReadOnly(true);
        try {
            assertThatThrownBy(() -> transactions.execute(status -> repository.submit(message())))
                    .isInstanceOf(IllegalStateException.class);
        } finally {
            transactions.setReadOnly(false);
        }
        assertThatThrownBy(() -> transactions.execute(status -> repository.submit(null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(repository.pendingCount()).isZero();
    }

    @Test
    void traceIdentifiersAreValidatedBeforePersistence() {
        String previous = MDC.get("traceId");
        try {
            MDC.put("traceId", "0123456789abcdef0123456789abcdef");
            String traced = publish();
            assertThat(jdbc.queryForObject("SELECT trace_id FROM loadup_task WHERE id = ?", String.class, traced))
                    .isEqualTo("0123456789abcdef0123456789abcdef");
            MDC.put("traceId", "invalid-request-controlled-value");
            String invalid = publish();
            assertThat(jdbc.queryForObject("SELECT trace_id FROM loadup_task WHERE id = ?", String.class, invalid))
                    .isNull();
            MDC.remove("traceId");
            String absent = publish();
            assertThat(jdbc.queryForObject("SELECT trace_id FROM loadup_task WHERE id = ?", String.class, absent))
                    .isNull();
        } finally {
            if (previous == null) {
                MDC.remove("traceId");
            } else {
                MDC.put("traceId", previous);
            }
        }
    }

    @Test
    void administrativeQueriesEnforceBoundsAndReplayOnlyFailedEvents() {
        assertThat(repository.failedCount()).isZero();
        assertThat(repository.oldestPendingSeconds()).isZero();
        assertThat(repository.find("missing")).isEmpty();
        assertThatThrownBy(() -> repository.failures(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> repository.failures(1001)).isInstanceOf(IllegalArgumentException.class);
        String pending = publish();
        assertThatThrownBy(() -> repository.retryFailed(pending, "operator", "reviewed"))
                .isInstanceOf(CommonException.class);
        var claim = repository.claim().orElseThrow();
        assertThat(repository.complete(claim)).isTrue();
        assertThatThrownBy(() -> repository.retryFailed(pending, "operator", "reviewed"))
                .isInstanceOf(CommonException.class);
        String failed = publish();
        jdbc.update(
                "UPDATE loadup_task SET status = 'FAILED', attempts = ? WHERE id = ?",
                properties.getMaxAttempts(),
                failed);
        assertThat(repository.failedCount()).isEqualTo(1);
        assertThat(repository.failures(1)).extracting(TaskExecution::id).containsExactly(failed);
        repository.retryFailed(failed, "operator", "approved retry");
        assertThat(repository.find(failed).orElseThrow().attempts()).isZero();
        assertThat(repository.failedCount()).isZero();
        assertThat(repository.pendingCount()).isEqualTo(1);
        jdbc.update(
                "UPDATE loadup_task SET created_at = (clock_timestamp() + INTERVAL '-30 seconds') WHERE id = ?",
                failed);
        assertThat(repository.oldestPendingSeconds()).isGreaterThanOrEqualTo(30);
        jdbc.update(
                "UPDATE loadup_task SET created_at = (clock_timestamp() + INTERVAL '30 seconds') WHERE id = ?", failed);
        assertThat(repository.oldestPendingSeconds()).isZero();
    }

    @Test
    void handlerFailuresScheduleRetriesThenExhaustTheAttemptBudget() {
        String id = publish();
        var first = repository.claim().orElseThrow();
        repository.fail(first, new IllegalStateException("sensitive detail"));
        TaskExecution retry = repository.find(id).orElseThrow();
        assertThat(retry.status()).isEqualTo(TaskStatus.PENDING);
        assertThat(retry.attempts()).isEqualTo(1);
        assertThat(retry.lastError()).isEqualTo("IllegalStateException");
        assertThat(repository.claim()).isEmpty();
        jdbc.update(
                "UPDATE loadup_task SET attempts = ?, next_execute_at = CURRENT_TIMESTAMP(6) WHERE id = ?",
                properties.getMaxAttempts() - 1,
                id);
        var last = repository.claim().orElseThrow();
        repository.fail(last, new IllegalArgumentException("sensitive detail"));
        var failed = repository.find(id).orElseThrow();
        assertThat(failed.status()).isEqualTo(TaskStatus.FAILED);
        assertThat(failed.nextExecuteAt()).isEqualTo(last.event().nextExecuteAt());
        assertThat(repository.failedCount()).isEqualTo(1);
        assertThat(repository.pendingCount()).isZero();
    }

    @Test
    void plannedTasksDeduplicateAndCancellationFencesCompletion() {
        var deadline = LocalDateTime.now().plusDays(1).withNano(0);
        var submission = TaskSubmission.scheduled("order.expire", "order", deadline);
        String id = transactions.execute(tx -> repository.submit(submission));
        String duplicate = transactions.execute(
                tx -> repository.submit(TaskSubmission.scheduled("order.expire", "order", deadline.plusDays(1))));
        assertThat(duplicate).isEqualTo(id);
        assertThat(repository.find(id).orElseThrow().nextExecuteAt()).isEqualTo(deadline);
        assertThat(repository.claim()).isEmpty();
        assertThatThrownBy(() -> transactions.execute(
                        tx -> repository.submit(new TaskSubmission("order.expire", "order", "different", deadline))))
                .isInstanceOf(CommonException.class);
        repository.reschedule(id, LocalDateTime.now().minusMinutes(1));
        var claim = repository.claim().orElseThrow();
        assertThat(repository.cancel(id)).isTrue();
        assertThat(repository.complete(claim)).isFalse();
        repository.reschedule(id, deadline);
        assertThat(repository.find(id).orElseThrow().nextExecuteAt()).isEqualTo(deadline);
        assertThat(repository.find(id).orElseThrow().attempts()).isZero();
    }

    private String publish() {
        return transactions.execute(status -> repository.submit(message()));
    }

    private TaskSubmission message() {
        return TaskSubmission.immediate("test.created", UUID.randomUUID().toString(), "{}");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {}
}
