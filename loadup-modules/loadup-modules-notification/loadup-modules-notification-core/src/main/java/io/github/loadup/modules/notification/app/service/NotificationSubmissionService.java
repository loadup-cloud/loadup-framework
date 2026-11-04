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
import io.github.loadup.components.database.id.IdGenerator;
import io.github.loadup.components.outbox.OutboxMessage;
import io.github.loadup.components.outbox.OutboxTemplate;
import io.github.loadup.modules.notification.app.converter.NotificationSubmissionConverter;
import io.github.loadup.modules.notification.client.command.NotificationSubmitCommand;
import io.github.loadup.modules.notification.client.dto.NotificationAcceptedDTO;
import io.github.loadup.modules.notification.client.error.NotificationResultCodeEnum;
import io.github.loadup.modules.notification.client.facade.NotificationFacade;
import io.github.loadup.modules.notification.domain.gateway.NotificationSubmissionGateway;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

public class NotificationSubmissionService implements NotificationFacade {
    public static final String DELIVERY_EVENT = "notification.in-app.delivery";
    private final NotificationSubmissionGateway gateway;
    private final OutboxTemplate outbox;
    private final IdGenerator ids;
    private final JsonMapper json;
    private final NotificationSubmissionConverter converter;
    private final Clock clock;

    public NotificationSubmissionService(
            NotificationSubmissionGateway gateway,
            OutboxTemplate outbox,
            IdGenerator ids,
            JsonMapper json,
            NotificationSubmissionConverter converter,
            Clock clock) {
        this.gateway = gateway;
        this.outbox = outbox;
        this.ids = ids;
        this.json = json;
        this.converter = converter;
        this.clock = clock;
    }

    @Override
    @Transactional
    public NotificationAcceptedDTO submit(String source, NotificationSubmitCommand command) {
        if (source == null || source.isBlank() || source.length() > 128 || command == null) {
            throw new CommonException(NotificationResultCodeEnum.INVALID_SUBMISSION);
        }
        var content = converter.toContent(command);
        String payload = json.writeValueAsString(content);
        String digest = digest(payload);
        var at = LocalDateTime.ofInstant(clock.instant(), clock.getZone()).truncatedTo(ChronoUnit.MICROS);
        String id = ids.generate();
        boolean inserted = gateway.insert(id, source, command.requestKey(), digest, payload, at);
        var receipt = gateway.find(source, command.requestKey());
        if (!digest.equals(receipt.contentDigest())) {
            throw new CommonException(NotificationResultCodeEnum.REQUEST_CONFLICT);
        }
        if (!inserted) {
            return converter.toAccepted(receipt, true);
        }
        if (content.expiresAt() != null && !content.expiresAt().isAfter(at)) {
            throw new CommonException(NotificationResultCodeEnum.NOTIFICATION_EXPIRED);
        }
        for (String recipient : content.recipients()) {
            String deliveryId = ids.generate();
            String eventId = outbox.publish(new OutboxMessage(DELIVERY_EVENT, 1, deliveryId, deliveryId));
            gateway.addDelivery(deliveryId, receipt.notificationId(), recipient, eventId, at);
        }
        return converter.toAccepted(receipt, false);
    }

    private static String digest(String value) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new AssertionError(impossible);
        }
    }
}
