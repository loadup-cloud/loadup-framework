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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(classes = OutboxIT.Application.class)
@ActiveProfiles("test")
@EnableTestContainers(ContainerType.MYSQL)
class OutboxIT {
    private final JdbcOutboxRepository repository;
    private final OutboxInbox inbox;
    private final OutboxProperties properties;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    @Autowired
    OutboxIT(JdbcOutboxRepository repository, OutboxInbox inbox, OutboxProperties properties, DataSource source) {
        this.repository = repository;
        this.inbox = inbox;
        this.properties = properties;
        jdbc = new JdbcTemplate(source);
        transactions = new TransactionTemplate(new JdbcTransactionManager(source));
    }

    @BeforeEach
    void reset() {
        jdbc.update("DELETE FROM loadup_outbox_inbox");
        jdbc.update("DELETE FROM loadup_outbox_replay");
        jdbc.update("DELETE FROM loadup_outbox_event");
        TenantUtil.clear();
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
                "UPDATE loadup_outbox_event SET lease_until = TIMESTAMPADD(SECOND, -1, UTC_TIMESTAMP(6)) WHERE id = ?",
                id);
        var recovered = repository.claim().orElseThrow();
        assertThat(recovered.event().id()).isEqualTo(id);
        assertThat(recovered.token()).isNotEqualTo(first.token());
        assertThat(repository.complete(first)).isFalse();
        assertThat(repository.complete(recovered)).isTrue();
    }

    @Test
    void exhaustedLeaseBecomesFailedAndReplayIsTenantScopedAndAudited() {
        String id = publish();
        repository.claim().orElseThrow();
        jdbc.update(
                "UPDATE loadup_outbox_event SET attempts = ?, lease_until = TIMESTAMPADD(SECOND, -1, UTC_TIMESTAMP(6)) WHERE id = ?",
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
        assertThat(transactions.execute(status -> inbox.consume("consumer", event, work::incrementAndGet)))
                .isTrue();
        assertThat(transactions.execute(status -> inbox.consume("consumer", event, work::incrementAndGet)))
                .isFalse();
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
            TenantUtil.setTenantId("previous");
            try {
                var dispatcher = new OutboxDispatcher(repository, properties, List.of(handler), null);
                assertThat(dispatcher.dispatchBatch()).isEqualTo(1);
                jdbc.update("UPDATE loadup_outbox_event SET available_at = UTC_TIMESTAMP(6) WHERE id = ?", id);
                dispatcher.dispatchBatch();
                assertThat(dispatcher.dispatchBatch()).isZero();
                assertThat(calls).hasValue(2);
                assertThat(accepted).containsExactly(id);
                assertThat(repository.find("tenant", id).orElseThrow().status()).isEqualTo("SUCCEEDED");
                assertThat(TenantUtil.getTenantId()).isEqualTo("previous");
            } finally {
                TenantUtil.clear();
            }
        } finally {
            server.stop(0);
        }
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
