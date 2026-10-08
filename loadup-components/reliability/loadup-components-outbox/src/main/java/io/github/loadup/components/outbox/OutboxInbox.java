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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Deduplicates local database work in the caller's transaction; not external HTTP effects. */
public class OutboxInbox {
    private final DataSource dataSource;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;

    public OutboxInbox(DataSource dataSource) {
        this.dataSource = dataSource;
        jdbc = new JdbcTemplate(dataSource);
        transactions = new TransactionTemplate(new JdbcTransactionManager(dataSource));
        transactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_MANDATORY);
    }

    public boolean consume(String consumerId, OutboxEvent event, Runnable databaseWork) {
        OutboxMessage.required(consumerId, "consumerId", 128);
        if (event == null || databaseWork == null)
            throw new IllegalArgumentException("event and databaseWork are required");
        JdbcOutboxRepository.requireTransaction(dataSource);
        return Boolean.TRUE.equals(transactions.execute(status -> {
            try {
                jdbc.update(
                        "INSERT INTO loadup_outbox_inbox (id, tenant_id, created_at, updated_at, consumer_id, event_id) "
                                + "VALUES (?, ?, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), ?, ?)",
                        UUID.randomUUID().toString(),
                        event.message().tenantId(),
                        consumerId,
                        event.id());
            } catch (DuplicateKeyException duplicate) {
                Integer count = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM loadup_outbox_inbox WHERE tenant_id = ? AND consumer_id = ? AND event_id = ?",
                        Integer.class,
                        event.message().tenantId(),
                        consumerId,
                        event.id());
                if (count == null || count == 0) throw duplicate;
                return false;
            }
            databaseWork.run();
            return true;
        }));
    }
}
