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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class InboxGatewayImpl implements InboxGateway {
    private final NamedParameterJdbcTemplate jdbc;
    private final NotificationStorageConverter converter;
    private final IdGenerator ids;
    private final Clock clock;

    public InboxGatewayImpl(
            NamedParameterJdbcTemplate jdbc, NotificationStorageConverter converter, Clock clock, IdGenerator ids) {
        this.jdbc = jdbc;
        this.converter = converter;
        this.clock = clock;
        this.ids = ids;
    }

    private MapSqlParameterSource args(String tenant, String recipient) {
        return new MapSqlParameterSource("tenant", tenant).addValue("recipient", recipient);
    }

    private long count(String predicate, MapSqlParameterSource args) {
        return jdbc.queryForObject(
                "SELECT COUNT(*) FROM notification_inbox WHERE tenant_id = :tenant AND recipient_id = :recipient AND deleted = FALSE "
                        + predicate,
                args,
                Long.class);
    }

    @Override
    public int insert(InboxMessage message, String requestKey) {
        var row = converter.toDO(message);
        row.setRequestKey(requestKey);
        if (row.getId() == null || row.getId().isBlank()) row.setId(ids.generate());
        var now = LocalDateTime.now(clock);
        if (row.getCreatedAt() == null) row.setCreatedAt(now);
        row.setUpdatedAt(now);
        return jdbc.update("""
                INSERT INTO notification_inbox (id, tenant_id, recipient_id, sender_id, category, title, body, action_url,
                    request_key, read_at, archived_at, created_at, updated_at, deleted)
                VALUES (:id, :tenantId, :recipientId, :senderId, :category, :title, :body, :actionUrl,
                    :requestKey, :readAt, :archivedAt, :createdAt, :updatedAt, FALSE)
                ON CONFLICT (tenant_id, recipient_id, request_key) DO NOTHING
                """, new BeanPropertySqlParameterSource(row));
    }

    @Override
    public PageResult<InboxMessage> list(String tenant, String recipient, boolean unread, int page, int size) {
        var args = args(tenant, recipient).addValue("limit", size).addValue("offset", (long) (page - 1) * size);
        String predicate = " AND archived_at IS NULL" + (unread ? " AND read_at IS NULL" : "");
        long total = count(predicate, args);
        var rows = jdbc.query(
                "SELECT * FROM notification_inbox WHERE tenant_id = :tenant AND recipient_id = :recipient AND deleted = FALSE "
                        + predicate + " ORDER BY created_at DESC, id DESC LIMIT :limit OFFSET :offset",
                args,
                BeanPropertyRowMapper.newInstance(InboxMessageDO.class));
        return new PageResult<>(rows.stream().map(converter::toDomain).toList(), total, page, size);
    }

    @Override
    public long unreadCount(String tenant, String recipient) {
        return count(" AND archived_at IS NULL AND read_at IS NULL", args(tenant, recipient));
    }

    @Override
    public int markRead(String tenant, String recipient, String id) {
        var args = args(tenant, recipient).addValue("id", id).addValue("now", LocalDateTime.now(clock));
        int changed = jdbc.update(
                "UPDATE notification_inbox SET read_at = :now, updated_at = :now WHERE tenant_id = :tenant AND recipient_id = :recipient "
                        + "AND id = :id AND deleted = FALSE AND archived_at IS NULL AND read_at IS NULL",
                args);
        return changed > 0 ? changed : (int) count(" AND id = :id AND archived_at IS NULL", args);
    }

    @Override
    public int markAllRead(String tenant, String recipient) {
        return jdbc.update(
                "UPDATE notification_inbox SET read_at = :now, updated_at = :now WHERE tenant_id = :tenant AND recipient_id = :recipient "
                        + "AND deleted = FALSE AND archived_at IS NULL AND read_at IS NULL",
                args(tenant, recipient).addValue("now", LocalDateTime.now(clock)));
    }

    @Override
    public int archive(String tenant, String recipient, String id) {
        var args = args(tenant, recipient).addValue("id", id).addValue("now", LocalDateTime.now(clock));
        int changed = jdbc.update(
                "UPDATE notification_inbox SET archived_at = :now, updated_at = :now WHERE tenant_id = :tenant AND recipient_id = :recipient "
                        + "AND id = :id AND deleted = FALSE AND archived_at IS NULL",
                args);
        return changed > 0 ? changed : (int) count(" AND id = :id", args);
    }
}
