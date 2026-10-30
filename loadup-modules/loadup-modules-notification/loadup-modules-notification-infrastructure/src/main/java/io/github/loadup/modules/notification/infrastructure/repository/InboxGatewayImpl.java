/*
 * #%L
 * LoadUp Notification Infrastructure
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
package io.github.loadup.modules.notification.infrastructure.repository;

import io.github.loadup.commons.domain.PageResult;
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.modules.notification.domain.gateway.InboxGateway;
import io.github.loadup.modules.notification.domain.model.InboxMessage;
import io.github.loadup.modules.notification.infrastructure.converter.NotificationStorageConverter;
import io.github.loadup.modules.notification.infrastructure.dataobject.InboxMessageDO;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;

public class InboxGatewayImpl implements InboxGateway {
    private static final String COLUMNS =
            "id, recipient_id, sender_id, category, title, body, action_url, request_key, read_at, archived_at, created_at, updated_at, deleted";
    private static final String FROM = " FROM notification_inbox WHERE deleted = FALSE";

    private final JdbcClient jdbc;
    private final NotificationStorageConverter converter;
    private final IdGenerator ids;
    private final Clock clock;

    public InboxGatewayImpl(JdbcClient jdbc, NotificationStorageConverter converter, Clock clock, IdGenerator ids) {
        this.jdbc = jdbc;
        this.converter = converter;
        this.clock = clock;
        this.ids = ids;
    }

    @Override
    public int insert(InboxMessage message, String requestKey) {
        var row = converter.toDO(message);
        row.setRequestKey(requestKey);
        if (row.getId() == null || row.getId().isBlank()) {
            row.setId(ids.generate());
        }
        var now = LocalDateTime.now(clock);
        if (row.getCreatedAt() == null) {
            row.setCreatedAt(now);
        }
        row.setUpdatedAt(now);
        return jdbc.sql("""
                INSERT INTO notification_inbox (id, recipient_id, sender_id, category, title, body, action_url,
                    request_key, read_at, archived_at, created_at, updated_at, deleted)
                VALUES (:id, :recipientId, :senderId, :category, :title, :body, :actionUrl,
                    :requestKey, :readAt, :archivedAt, :createdAt, :updatedAt, FALSE)
                ON CONFLICT (recipient_id, request_key) DO NOTHING
                """)
                .paramSource(new BeanPropertySqlParameterSource(row))
                .update();
    }

    @Override
    public PageResult<InboxMessage> list(String recipient, boolean unread, int page, int size) {
        String predicate =
                " AND recipient_id = :recipient AND archived_at IS NULL" + (unread ? " AND read_at IS NULL" : "");
        long total = count(predicate, java.util.Map.of("recipient", recipient));
        var rows = jdbc.sql("SELECT " + COLUMNS + FROM + predicate
                        + " ORDER BY created_at DESC, id DESC LIMIT :limit OFFSET :offset")
                .param("recipient", recipient)
                .param("limit", size)
                .param("offset", (long) (page - 1) * size)
                .query(BeanPropertyRowMapper.newInstance(InboxMessageDO.class))
                .list();
        return new PageResult<>(rows.stream().map(converter::toDomain).toList(), total, page, size);
    }

    @Override
    public long unreadCount(String recipient) {
        return count(
                " AND recipient_id = :recipient AND archived_at IS NULL AND read_at IS NULL",
                java.util.Map.of("recipient", recipient));
    }

    @Override
    public int markRead(String recipient, String id) {
        var now = LocalDateTime.now(clock);
        int changed = jdbc.sql(
                        "UPDATE notification_inbox SET read_at = :now, updated_at = :now WHERE recipient_id = :recipient "
                                + "AND id = :id AND deleted = FALSE AND archived_at IS NULL AND read_at IS NULL")
                .param("now", now)
                .param("recipient", recipient)
                .param("id", id)
                .update();
        return changed > 0
                ? changed
                : (int) count(
                        " AND recipient_id = :recipient AND id = :id AND archived_at IS NULL",
                        java.util.Map.of("recipient", recipient, "id", id));
    }

    @Override
    public int markAllRead(String recipient) {
        var now = LocalDateTime.now(clock);
        return jdbc.sql(
                        "UPDATE notification_inbox SET read_at = :now, updated_at = :now WHERE recipient_id = :recipient "
                                + "AND deleted = FALSE AND archived_at IS NULL AND read_at IS NULL")
                .param("now", now)
                .param("recipient", recipient)
                .update();
    }

    @Override
    public int archive(String recipient, String id) {
        var now = LocalDateTime.now(clock);
        int changed = jdbc.sql(
                        "UPDATE notification_inbox SET archived_at = :now, updated_at = :now WHERE recipient_id = :recipient "
                                + "AND id = :id AND deleted = FALSE AND archived_at IS NULL")
                .param("now", now)
                .param("recipient", recipient)
                .param("id", id)
                .update();
        return changed > 0
                ? changed
                : (int) count(
                        " AND recipient_id = :recipient AND id = :id",
                        java.util.Map.of("recipient", recipient, "id", id));
    }

    private long count(String predicate, java.util.Map<String, ?> params) {
        Long count = jdbc.sql("SELECT COUNT(*)" + FROM + predicate)
                .params(params)
                .query(Long.class)
                .single();
        return count == null ? 0 : count;
    }
}
