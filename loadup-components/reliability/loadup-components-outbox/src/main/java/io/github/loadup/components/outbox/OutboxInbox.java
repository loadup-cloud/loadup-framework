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

import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Deduplicates local database work in the caller's transaction; not external HTTP effects. */
public class OutboxInbox {
    private final DataSource dataSource;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    public OutboxInbox(DataSource dataSource, PlatformTransactionManager manager) {
        this.dataSource = dataSource;
        jdbc = new JdbcTemplate(dataSource);
        JdbcOutboxRepository.requireManager(dataSource, manager);
        transactions = new TransactionTemplate(manager);
        transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);
    }

    public boolean consume(String consumerId, OutboxEvent event, Runnable databaseWork) {
        OutboxMessage.required(consumerId, "consumerId", 128);
        if (event == null || databaseWork == null)
            throw new IllegalArgumentException("event and databaseWork are required");
        JdbcOutboxRepository.requireTransaction(dataSource);
        return Boolean.TRUE.equals(transactions.execute(status -> {
            int inserted = jdbc.update(
                    "INSERT INTO loadup_outbox_inbox (id, tenant_id, created_at, updated_at, consumer_id, event_id) "
                            + "VALUES (?, ?, statement_timestamp(), statement_timestamp(), ?, ?) "
                            + "ON CONFLICT (tenant_id, consumer_id, event_id) DO NOTHING",
                    UUID.randomUUID().toString(),
                    event.message().tenantId(),
                    consumerId,
                    event.id());
            if (inserted == 0) return false;
            if (inserted != 1) throw new IllegalStateException("Unexpected inbox insert count");
            databaseWork.run();
            return true;
        }));
    }
}
