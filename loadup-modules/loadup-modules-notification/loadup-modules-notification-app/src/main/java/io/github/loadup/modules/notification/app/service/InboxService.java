/*
 * #%L
 * LoadUp Notification App
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
package io.github.loadup.modules.notification.app.service;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.notification.app.converter.NotificationDTOConverter;
import io.github.loadup.modules.notification.client.dto.InboxMessageDTO;
import io.github.loadup.modules.notification.client.error.NotificationResultCodeEnum;
import io.github.loadup.modules.notification.domain.gateway.InboxGateway;
import io.github.loadup.modules.notification.domain.model.InboxMessage;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

/** Delivery to a private, persistent inbox and recipient-owned read operations. */
public class InboxService implements io.github.loadup.modules.notification.client.facade.InboxFacade {
    private final Clock clock;
    private static final String DEFAULT_TENANT = "__default__";
    private final NotificationDTOConverter converter;
    private final InboxGateway repository;

    public InboxService(
            InboxGateway repository, NotificationDTOConverter converter, @Qualifier("notificationClock") Clock clock) {
        this.clock = clock;
        this.converter = converter;
        this.repository = repository;
    }

    @Transactional
    public int publish(
            String tenantId,
            String senderId,
            List<String> recipients,
            String category,
            String title,
            String body,
            String actionUrl,
            String requestKey) {
        String tenant = tenant(tenantId);
        if (recipients == null || recipients.isEmpty() || recipients.size() > 100) {
            throw new CommonException(NotificationResultCodeEnum.RECIPIENTS_MUST_CONTAIN_1_TO_100_USERS);
        }
        String sender = senderId == null || senderId.isBlank() ? null : required(senderId, 64);
        String kind = required(category, 64);
        String heading = required(title, 200);
        String text = required(body, 4000);
        String url = actionUrl == null || actionUrl.isBlank() ? null : required(actionUrl, 500);
        if (url != null
                && (!url.startsWith("/")
                        || url.startsWith("//")
                        || url.indexOf('\\') >= 0
                        || url.chars().anyMatch(Character::isISOControl))) {
            throw new CommonException(NotificationResultCodeEnum.INVALID_ACTION_URL);
        }
        String key = requestKey == null || requestKey.isBlank() ? null : required(requestKey, 128);
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        int inserted = 0;
        for (String recipient : recipients.stream().distinct().toList()) {
            InboxMessage message = new InboxMessage(
                    UUID.randomUUID().toString(),
                    tenant,
                    required(recipient, 64),
                    sender,
                    kind,
                    heading,
                    text,
                    url,
                    null,
                    now);
            inserted += repository.insert(message, key);
        }
        return inserted;
    }

    public PageDTO<InboxMessageDTO> list(String tenantId, String recipientId, boolean unreadOnly, int page, int size) {
        io.github.loadup.commons.dto.PageQuery.normalized(page, size, NotificationResultCodeEnum.INVALID_PAGE);
        return converter.toPageDTO(
                repository.list(tenant(tenantId), required(recipientId, 64), unreadOnly, page, size));
    }

    public long unreadCount(String tenantId, String recipientId) {
        return repository.unreadCount(tenant(tenantId), required(recipientId, 64));
    }

    public void markRead(String tenantId, String recipientId, String id) {
        if (repository.markRead(tenant(tenantId), required(recipientId, 64), required(id, 64)) == 0)
            throw new CommonException(NotificationResultCodeEnum.NOTIFICATION_NOT_FOUND);
    }

    public int markAllRead(String tenantId, String recipientId) {
        return repository.markAllRead(tenant(tenantId), required(recipientId, 64));
    }

    public void archive(String tenantId, String recipientId, String id) {
        if (repository.archive(tenant(tenantId), required(recipientId, 64), required(id, 64)) == 0)
            throw new CommonException(NotificationResultCodeEnum.NOTIFICATION_NOT_FOUND);
    }

    private static String tenant(String value) {
        return value == null || value.isBlank() ? DEFAULT_TENANT : required(value, 64);
    }

    private static String required(String value, int max) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
        }
        return value.trim();
    }
}
