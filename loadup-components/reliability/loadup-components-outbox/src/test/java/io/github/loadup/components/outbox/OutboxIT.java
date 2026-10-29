/*
 * #%L
 * LoadUp Outbox
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
package io.github.loadup.components.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.http.HttpCall;
import io.github.loadup.components.http.HttpProperties;
import io.github.loadup.components.http.HttpTemplate;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = OutboxIT.Application.class)
@ActiveProfiles("test")
@EnableTestContainers(value = ContainerType.POSTGRESQL, reuse = false)
class OutboxIT {
    private final JdbcOutboxRepository repository;
    private final OutboxInbox inbox;
    private final OutboxProperties properties;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    @Autowired
    OutboxIT(
            JdbcOutboxRepository repository,
            OutboxInbox inbox,
            OutboxProperties properties,
            DataSource source,
            PlatformTransactionManager manager) {
        this.repository = repository;
        this.inbox = inbox;
        this.properties = properties;
        jdbc = new JdbcTemplate(source);
        transactions = new TransactionTemplate(manager);
    }

    @BeforeEach
    void reset() {
        jdbc.update("DELETE FROM loadup_outbox_inbox");
        jdbc.update("DELETE FROM loadup_outbox_replay");
        jdbc.update("DELETE FROM loadup_outbox_event");
    }

    @Test
    void publicationRequiresTransactionAndRollsBackWithBusinessWork() {
        assertThatThrownBy(() -> repository.publish(message())).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> transactions.execute(status -> {
                    repository.publish(message());
                    throw new IllegalStateException("rollback");
                }))
                .isInstanceOf(IllegalStateException.class);
        assertThat(repository.pendingCount()).isZero();
        String id = publish();
        assertThat(repository.find("tenant", id)).isPresent();
        assertThat(repository.find("other", id)).isEmpty();
    }

    @Test
    void expiredClaimIsRecoveredAndOldOwnerCannotAcknowledge() {
        String id = publish();
        var first = repository.claim().orElseThrow();
        jdbc.update(
                "UPDATE loadup_outbox_event SET lease_until = (clock_timestamp() + INTERVAL '-1 seconds') WHERE id = ?",
                id);
        var recovered = repository.claim().orElseThrow();
        assertThat(recovered.event().id()).isEqualTo(id);
        assertThat(recovered.token()).isNotEqualTo(first.token());
        assertThat(repository.complete(first)).isFalse();
        assertThat(repository.fail(first, new IllegalStateException("stale"))).isFalse();
        assertThat(repository.complete(recovered)).isTrue();
    }

    @Test
    void exhaustedLeaseBecomesFailedAndReplayIsTenantScopedAndAudited() {
        String id = publish();
        repository.claim().orElseThrow();
        jdbc.update(
                "UPDATE loadup_outbox_event SET attempts = ?, lease_until = (clock_timestamp() + INTERVAL '-1 seconds') WHERE id = ?",
                properties.getMaxAttempts(),
                id);
        assertThat(repository.claim()).isEmpty();
        assertThat(repository.failures("tenant", 10)).hasSize(1);
        assertThat(repository.replay("other", id, "operator", "reviewed")).isFalse();
        assertThat(repository.replay("tenant", id, "operator", "reviewed")).isTrue();
        assertThat(repository.claim().orElseThrow().event().id()).isEqualTo(id);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM loadup_outbox_replay", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void concurrentWorkersDoNotShareAnActiveClaim() throws Exception {
        publish();
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(repository::claim);
            var second = executor.submit(repository::claim);
            assertThat(List.of(first.get(), second.get()).stream()
                            .filter(java.util.Optional::isPresent)
                            .count())
                    .isEqualTo(1);
        }
    }

    @Test
    void inboxRollbackAllowsRetryAndCommittedConsumptionIsDeduplicated() {
        publish();
        OutboxEvent event = repository.claim().orElseThrow().event();
        assertThatThrownBy(() -> transactions.execute(status -> inbox.consume("consumer", event, () -> {
                    throw new IllegalStateException("rollback");
                })))
                .isInstanceOf(IllegalStateException.class);
        AtomicInteger work = new AtomicInteger();
        Boolean consumed = transactions.execute(status -> inbox.consume("consumer", event, work::incrementAndGet));
        assertThat(consumed).isTrue();
        Boolean duplicate = transactions.execute(status -> inbox.consume("consumer", event, work::incrementAndGet));
        assertThat(duplicate).isFalse();
        assertThat(work).hasValue(1);
    }

    @Test
    void committedEventDrivesHttpAndRestoresTenantContext() throws Exception {
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
            OutboxHandler handler = new OutboxHandler() {
                public String eventType() {
                    return "test.created";
                }

                public void handle(OutboxEvent event) {
                    assertThat(TenantUtil.getTenantId()).isEqualTo("tenant");
                    var response = http.exchange(
                            "merchant",
                            "notify",
                            new HttpCall(
                                    null,
                                    null,
                                    Map.of("Idempotency-Key", event.id(), "Content-Type", "application/json"),
                                    event.message().payload()));
                    if (!response.getStatusCode().is2xxSuccessful())
                        throw new IllegalStateException("Notification rejected");
                    if (calls.get() == 1) throw new IllegalStateException("Simulated response loss after acceptance");
                }
            };
            TenantUtil.runWithTenant("previous", () -> {
                var dispatcher = new OutboxDispatcher(repository, properties, List.of(handler), null);
                assertThat(dispatcher.dispatchBatch()).isEqualTo(1);
                jdbc.update("UPDATE loadup_outbox_event SET available_at = CURRENT_TIMESTAMP(6) WHERE id = ?", id);
                dispatcher.dispatchBatch();
                assertThat(dispatcher.dispatchBatch()).isZero();
                assertThat(calls).hasValue(2);
                assertThat(accepted).containsExactly(id);
                assertThat(repository.find("tenant", id).orElseThrow().status()).isEqualTo("SUCCEEDED");
                assertThat(TenantUtil.getTenantId()).isEqualTo("previous");
            });
        } finally {
            server.stop(0);
        }
    }

    @Test
    void readOnlyTransactionsAndMissingArgumentsCannotPublishOrConsume() {
        transactions.setReadOnly(true);
        try {
            assertThatThrownBy(() -> transactions.execute(status -> repository.publish(message())))
                    .isInstanceOf(IllegalStateException.class);
        } finally {
            transactions.setReadOnly(false);
        }
        assertThatThrownBy(() -> transactions.execute(status -> repository.publish(null)))
                .isInstanceOf(IllegalArgumentException.class);
        OutboxEvent event = new OutboxEvent("event", message(), Instant.now(), null);
        assertThatThrownBy(() -> inbox.consume("consumer", event, () -> {})).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> inbox.consume(" ", event, () -> {})).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> inbox.consume("consumer", null, () -> {}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> inbox.consume("consumer", event, null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(repository.pendingCount()).isZero();
    }

    @Test
    void traceIdentifiersAreValidatedBeforePersistence() {
        String previous = MDC.get("traceId");
        try {
            MDC.put("traceId", "0123456789abcdef0123456789abcdef");
            String traced = publish();
            assertThat(jdbc.queryForObject(
                            "SELECT trace_id FROM loadup_outbox_event WHERE id = ?", String.class, traced))
                    .isEqualTo("0123456789abcdef0123456789abcdef");
            MDC.put("traceId", "invalid-request-controlled-value");
            String invalid = publish();
            assertThat(jdbc.queryForObject(
                            "SELECT trace_id FROM loadup_outbox_event WHERE id = ?", String.class, invalid))
                    .isNull();
            MDC.remove("traceId");
            String absent = publish();
            assertThat(jdbc.queryForObject(
                            "SELECT trace_id FROM loadup_outbox_event WHERE id = ?", String.class, absent))
                    .isNull();
        } finally {
            if (previous == null) MDC.remove("traceId");
            else MDC.put("traceId", previous);
        }
    }

    @Test
    void administrativeQueriesEnforceBoundsAndReplayOnlyFailedTenantEvents() {
        assertThat(repository.failedCount()).isZero();
        assertThat(repository.oldestPendingSeconds()).isZero();
        assertThat(repository.find("tenant", "missing")).isEmpty();
        assertThatThrownBy(() -> repository.failures("tenant", 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> repository.failures("tenant", 1001)).isInstanceOf(IllegalArgumentException.class);
        String pending = publish();
        assertThat(repository.replay("tenant", pending, "operator", "reviewed")).isFalse();
        var claim = repository.claim().orElseThrow();
        assertThat(repository.complete(claim)).isTrue();
        assertThat(repository.replay("tenant", pending, "operator", "reviewed")).isFalse();
        String failed = publish();
        jdbc.update(
                "UPDATE loadup_outbox_event SET status = 'FAILED', attempts = ? WHERE id = ?",
                properties.getMaxAttempts(),
                failed);
        assertThat(repository.failedCount()).isEqualTo(1);
        assertThat(repository.failures("tenant", 1))
                .extracting(OutboxRecord::id)
                .containsExactly(failed);
        assertThat(repository.failures("other", 1000)).isEmpty();
        assertThat(repository.replay("tenant", failed, "operator", "approved retry"))
                .isTrue();
        assertThat(repository.find("tenant", failed).orElseThrow().attempts()).isZero();
        assertThat(repository.failedCount()).isZero();
        assertThat(repository.pendingCount()).isEqualTo(1);
        jdbc.update(
                "UPDATE loadup_outbox_event SET created_at = (clock_timestamp() + INTERVAL '-30 seconds') WHERE id = ?",
                failed);
        assertThat(repository.oldestPendingSeconds()).isGreaterThanOrEqualTo(30);
        jdbc.update(
                "UPDATE loadup_outbox_event SET created_at = (clock_timestamp() + INTERVAL '30 seconds') WHERE id = ?",
                failed);
        assertThat(repository.oldestPendingSeconds()).isZero();
    }

    @Test
    void duplicateInboxRecordDoesNotAbortTheEnclosingPostgresqlTransaction() {
        var event = new OutboxEvent("duplicate-event", message(), Instant.now(), null);
        transactions.executeWithoutResult(status -> {
            assertThat(inbox.consume("consumer", event, () -> {})).isTrue();
            assertThat(inbox.consume("consumer", event, () -> {})).isFalse();
            repository.publish(message());
        });
        assertThat(repository.pendingCount()).isEqualTo(1);
    }

    @Test
    void inboxDeduplicationIsScopedToBothTenantAndConsumer() {
        String eventId = "shared-event-id";
        OutboxEvent first = new OutboxEvent(eventId, message(), Instant.now(), null);
        OutboxEvent other = new OutboxEvent(
                eventId, new OutboxMessage("other", "test.created", 1, "business", "{}"), Instant.now(), null);
        AtomicInteger calls = new AtomicInteger();
        Boolean consumed = transactions.execute(status -> inbox.consume("consumer", first, calls::incrementAndGet));
        Boolean secondConsumer =
                transactions.execute(status -> inbox.consume("other-consumer", first, calls::incrementAndGet));
        Boolean secondTenant = transactions.execute(status -> inbox.consume("consumer", other, calls::incrementAndGet));
        Boolean duplicate = transactions.execute(status -> inbox.consume("consumer", first, calls::incrementAndGet));
        assertThat(consumed).isTrue();
        assertThat(secondConsumer).isTrue();
        assertThat(secondTenant).isTrue();
        assertThat(duplicate).isFalse();
        assertThat(calls).hasValue(3);
    }

    @Test
    void handlerFailuresScheduleRetriesThenExhaustTheAttemptBudget() {
        String id = publish();
        var first = repository.claim().orElseThrow();
        repository.fail(first, new IllegalStateException("sensitive detail"));
        OutboxRecord retry = repository.find("tenant", id).orElseThrow();
        assertThat(retry.status()).isEqualTo("PENDING");
        assertThat(retry.attempts()).isEqualTo(1);
        assertThat(retry.lastError()).isEqualTo("IllegalStateException");
        assertThat(repository.claim()).isEmpty();
        jdbc.update(
                "UPDATE loadup_outbox_event SET attempts = ?, available_at = CURRENT_TIMESTAMP(6) WHERE id = ?",
                properties.getMaxAttempts() - 1,
                id);
        var last = repository.claim().orElseThrow();
        repository.fail(last, new IllegalArgumentException("sensitive detail"));
        assertThat(repository.find("tenant", id).orElseThrow().status()).isEqualTo("FAILED");
        assertThat(repository.failedCount()).isEqualTo(1);
        assertThat(repository.pendingCount()).isZero();
    }

    private String publish() {
        return transactions.execute(status -> repository.publish(message()));
    }

    private OutboxMessage message() {
        return new OutboxMessage("tenant", "test.created", 1, UUID.randomUUID().toString(), "{}");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class Application {}
}
