/*
 * Copyright (C) 2026 LoadUp Cloud
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
package io.github.loadup.modules.notification.infrastructure.repository;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.modules.notification.client.error.NotificationResultCodeEnum;
import io.github.loadup.modules.notification.domain.gateway.NotificationSubmissionGateway;
import io.github.loadup.modules.notification.domain.model.NotificationDelivery;
import io.github.loadup.modules.notification.domain.model.NotificationReceipt;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;

public class JdbcNotificationSubmissionGateway implements NotificationSubmissionGateway {
    private final JdbcClient jdbc;

    public JdbcNotificationSubmissionGateway(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean insert(
            String id, String source, String requestKey, String digest, String contentJson, LocalDateTime at) {
        return jdbc.sql("""
                INSERT INTO notification_request
                    (id, source, request_key, content_digest, content_json, created_at, updated_at)
                VALUES (:id, :source, :key, :digest, :content, :at, :at)
                ON CONFLICT (source, request_key) DO NOTHING
                """)
                        .param("id", id)
                        .param("source", source)
                        .param("key", requestKey)
                        .param("digest", digest)
                        .param("content", contentJson)
                        .param("at", at)
                        .update()
                == 1;
    }

    @Override
    public NotificationReceipt find(String source, String requestKey) {
        return jdbc.sql("""
                SELECT id AS notification_id, content_digest, created_at AS accepted_at
                FROM notification_request WHERE source = :source AND request_key = :key AND deleted = FALSE
                """)
                .param("source", source)
                .param("key", requestKey)
                .query(NotificationReceipt.class)
                .single();
    }

    @Override
    public void addDelivery(String id, String notificationId, String recipientId, String eventId, LocalDateTime at) {
        int changed = jdbc.sql("""
                INSERT INTO notification_delivery
                    (id, notification_id, recipient_id, channel, event_id, status, created_at, updated_at)
                VALUES (:id, :notification, :recipient, 'IN_APP', :event, 'PENDING', :at, :at)
                """)
                .param("id", id)
                .param("notification", notificationId)
                .param("recipient", recipientId)
                .param("event", eventId)
                .param("at", at)
                .update();
        requireChanged(changed);
    }

    @Override
    public Optional<NotificationDelivery> lockDelivery(String id) {
        return jdbc.sql("""
            SELECT d.id, d.notification_id, d.recipient_id, d.event_id, d.status, r.content_json
            FROM notification_delivery d JOIN notification_request r ON r.id = d.notification_id
            WHERE d.id = :id AND d.deleted = FALSE AND r.deleted = FALSE
            FOR UPDATE OF d
            """).param("id", id).query(NotificationDelivery.class).optional();
    }

    @Override
    public void finish(String id, String status, LocalDateTime at) {
        if (!"DELIVERED".equals(status) && !"EXPIRED".equals(status)) {
            throw new CommonException(NotificationResultCodeEnum.INVALID_DELIVERY_EVENT);
        }
        requireChanged(jdbc.sql("""
            UPDATE notification_delivery SET status = :status, updated_at = :at
            WHERE id = :id AND status = 'PENDING' AND deleted = FALSE
            """)
                .param("status", status)
                .param("at", at)
                .param("id", id)
                .update());
    }

    private static void requireChanged(int changed) {
        if (changed != 1) {
            throw new CommonException(NotificationResultCodeEnum.INVALID_DELIVERY_EVENT);
        }
    }
}
