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
package io.github.loadup.modules.notification.domain.gateway;

import io.github.loadup.modules.notification.domain.model.NotificationDelivery;
import io.github.loadup.modules.notification.domain.model.NotificationReceipt;
import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationSubmissionGateway {
    boolean insert(String id, String source, String requestKey, String digest, String contentJson, LocalDateTime at);

    NotificationReceipt find(String source, String requestKey);

    void addDelivery(String id, String notificationId, String recipientId, String eventId, LocalDateTime at);

    Optional<NotificationDelivery> lockDelivery(String id);

    void finish(String id, String status, LocalDateTime at);
}
