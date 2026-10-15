/*
 * #%L
 * Loadup Gotone Test
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
package io.github.loadup.components.gotone.test.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.gotone.config.ChannelConfigProvider.ChannelConfig;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.record.RecordHandler.Attempt;
import io.github.loadup.components.gotone.record.RecordHandler.SendAttemptResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class GotoneModelContractTest {
    @Test
    void channelRequestsDefaultCollectionsAndRequireContent() {
        var request = ChannelSendRequest.builder().content("receipt").build();
        assertThat(request.receivers()).isEmpty();
        assertThat(request.channelConfig()).isEmpty();
        assertThat(request.templateParams()).isEmpty();
        assertThatThrownBy(() -> ChannelSendRequest.builder().build()).isInstanceOf(NullPointerException.class);
    }

    @Test
    void requestsDefensivelyCopyMutableCallerInputs() {
        var receivers = new ArrayList<>(List.of("buyer@example.test"));
        var parameters = new HashMap<String, Object>(Map.of("amount", "10.00"));
        var channels = new ArrayList<>(List.of("EMAIL"));
        var notification = NotificationRequest.builder()
                .serviceCode("PAYMENT_RECEIPT")
                .receivers(receivers)
                .templateParams(parameters)
                .requestId("request-1")
                .channels(channels)
                .async(true)
                .build();
        var channel = ChannelSendRequest.builder()
                .receivers(receivers)
                .content("receipt")
                .channelConfig(parameters)
                .templateParams(parameters)
                .build();
        receivers.clear();
        parameters.clear();
        channels.clear();
        assertThat(notification.receivers()).containsExactly("buyer@example.test");
        assertThat(notification.templateParams()).containsEntry("amount", "10.00");
        assertThat(notification.channels()).containsExactly("EMAIL");
        assertThat(notification.requestId()).isEqualTo("request-1");
        assertThat(notification.async()).isTrue();
        assertThat(channel.receivers()).containsExactly("buyer@example.test");
        assertThat(channel.channelConfig()).containsEntry("amount", "10.00");
        assertThat(channel.templateParams()).containsEntry("amount", "10.00");
        assertThatThrownBy(() -> notification.receivers().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> channel.channelConfig().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void notificationsRejectMissingServiceAndEmptyRecipients() {
        assertThatThrownBy(() -> NotificationRequest.of(null, List.of("buyer@example.test"), null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> NotificationRequest.of("receipt", null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NotificationRequest.of("receipt", List.of(), null))
                .isInstanceOf(IllegalArgumentException.class);
        var valid = NotificationRequest.of("receipt", List.of("buyer@example.test"), null);
        assertThat(valid.templateParams()).isEmpty();
        assertThat(valid.channels()).isEmpty();
        assertThat(valid.async()).isNull();
    }

    @Test
    void channelResponseSeparatesMissingFailedAndSuccessfulRecipients() {
        var empty = new ChannelSendResponse("receipt", 0, 0, null, null);
        assertThat(empty.isSuccess()).isFalse();
        assertThat(empty.isSuccess("absent")).isFalse();
        assertThat(empty.getErrorMessage("absent")).isNull();
        var statuses = new HashMap<>(Map.of("sent", true, "failed", false));
        var errors = new HashMap<>(Map.of("failed", "Provider unavailable"));
        var mixed = new ChannelSendResponse("receipt", 1, 1, statuses, errors);
        statuses.clear();
        errors.clear();
        assertThat(mixed.isSuccess()).isTrue();
        assertThat(mixed.isSuccess("sent")).isTrue();
        assertThat(mixed.isSuccess("failed")).isFalse();
        assertThat(mixed.getErrorMessage("failed")).isEqualTo("Provider unavailable");
        assertThatThrownBy(() -> mixed.receiverStatus().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void optionalChannelConfigurationDefaultsAndCopiesFallbacks() {
        var empty = new ChannelConfig("EMAIL", "smtp", null, null, null);
        assertThat(empty.fallbackProviders()).isEmpty();
        assertThat(empty.channelConfig()).isEmpty();
        var fallbacks = new ArrayList<>(List.of("backup"));
        var settings = new HashMap<String, Object>(Map.of("subject", "receipt"));
        var configured = new ChannelConfig("EMAIL", "smtp", fallbacks, "${amount}", settings);
        fallbacks.clear();
        settings.clear();
        assertThat(configured.fallbackProviders()).containsExactly("backup");
        assertThat(configured.channelConfig()).containsEntry("subject", "receipt");
    }

    @Test
    void persistedAttemptSnapshotDefaultsAndProtectsAuditInputs() {
        var empty = new SendAttemptResult("smtp", false, 0, 1, null, null, null, null);
        assertThat(empty.content()).isEmpty();
        assertThat(empty.receiverStatus()).isEmpty();
        assertThat(empty.receiverErrors()).isEmpty();
        assertThat(empty.attempts()).isEmpty();
        var attempts = new ArrayList<>(
                List.of(new Attempt("primary", false, "unavailable"), new Attempt("backup", true, null)));
        var statuses = new HashMap<>(Map.of("buyer", true));
        var errors = new HashMap<>(Map.of("other", "unavailable"));
        var snapshot = new SendAttemptResult("backup", true, 1, 1, "receipt", statuses, errors, attempts);
        statuses.clear();
        errors.clear();
        attempts.clear();
        assertThat(snapshot.content()).isEqualTo("receipt");
        assertThat(snapshot.receiverStatus()).containsEntry("buyer", true);
        assertThat(snapshot.receiverErrors()).containsEntry("other", "unavailable");
        assertThat(snapshot.attempts()).extracting(Attempt::providerName).containsExactly("primary", "backup");
        assertThatThrownBy(() -> snapshot.attempts().clear()).isInstanceOf(UnsupportedOperationException.class);
    }
}
