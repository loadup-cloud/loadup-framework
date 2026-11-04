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
package io.github.loadup.modules.notification.app.service;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.outbox.OutboxEvent;
import io.github.loadup.components.outbox.OutboxHandler;
import io.github.loadup.modules.notification.client.error.NotificationResultCodeEnum;
import io.github.loadup.modules.notification.client.facade.InboxFacade;
import io.github.loadup.modules.notification.domain.gateway.NotificationSubmissionGateway;
import io.github.loadup.modules.notification.domain.model.NotificationContent;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

public class InAppDeliveryHandler implements OutboxHandler {
    private final NotificationSubmissionGateway gateway;
    private final InboxFacade inbox;
    private final JsonMapper json;
    private final Clock clock;

    public InAppDeliveryHandler(
            NotificationSubmissionGateway gateway, InboxFacade inbox, JsonMapper json, Clock clock) {
        this.gateway = gateway;
        this.inbox = inbox;
        this.json = json;
        this.clock = clock;
    }

    @Override
    public String eventType() {
        return NotificationSubmissionService.DELIVERY_EVENT;
    }

    @Override
    @Transactional
    public void handle(OutboxEvent event) {
        if (event == null
                || event.message() == null
                || event.message().version() != 1
                || !Objects.equals(eventType(), event.message().type())
                || !Objects.equals(event.message().businessId(), event.message().payload())) {
            throw new CommonException(NotificationResultCodeEnum.INVALID_DELIVERY_EVENT);
        }
        var delivery = gateway.lockDelivery(event.message().businessId())
                .orElseThrow(() -> new CommonException(NotificationResultCodeEnum.INVALID_DELIVERY_EVENT));
        if (!Objects.equals(event.id(), delivery.eventId())) {
            throw new CommonException(NotificationResultCodeEnum.INVALID_DELIVERY_EVENT);
        }
        if (!"PENDING".equals(delivery.status())) {
            return;
        }
        var content = json.readValue(delivery.contentJson(), NotificationContent.class);
        var at = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        if (content.expiresAt() != null && !content.expiresAt().isAfter(at)) {
            gateway.finish(delivery.id(), "EXPIRED", at);
            return;
        }
        if (!"IN_APP".equals(content.channel()) || !content.recipients().contains(delivery.recipientId())) {
            throw new CommonException(NotificationResultCodeEnum.INVALID_DELIVERY_EVENT);
        }
        inbox.publish(
                null,
                List.of(delivery.recipientId()),
                content.sceneCode(),
                content.title(),
                content.body(),
                content.actionUrl(),
                "delivery:" + delivery.id());
        gateway.finish(delivery.id(), "DELIVERED", at);
    }
}
