/*
 * #%L
 * LoadUp Notification Client
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
package io.github.loadup.modules.notification.client.facade;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.notification.client.dto.InboxMessageDTO;
import java.util.List;

/** Public business contract for InboxService. */
public interface InboxFacade {
    int publish(
            String tenantId,
            String senderId,
            List<String> recipients,
            String category,
            String title,
            String body,
            String actionUrl,
            String requestKey);

    PageDTO<InboxMessageDTO> list(String tenantId, String recipientId, boolean unreadOnly, int page, int size);

    long unreadCount(String tenantId, String recipientId);

    void markRead(String tenantId, String recipientId, String id);

    int markAllRead(String tenantId, String recipientId);

    void archive(String tenantId, String recipientId, String id);
}
