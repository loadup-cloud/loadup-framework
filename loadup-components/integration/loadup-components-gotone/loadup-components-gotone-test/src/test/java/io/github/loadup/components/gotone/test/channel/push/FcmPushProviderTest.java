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
package io.github.loadup.components.gotone.test.channel.push;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.gotone.channel.push.FcmPushProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FcmPushProviderTest {
    private static final String TOKEN = "device_token-1234567890";
    private static final String OTHER_TOKEN = "another_token-1234567890";

    @Test
    void identifiesFcmAndAvailabilityWithoutClaimingTransportDelivery() {
        var provider = new FcmPushProvider("synthetic-key", "project-one");
        assertThat(provider.getChannelType()).isEqualTo("PUSH");
        assertThat(provider.getProviderName()).isEqualTo("fcm");
        assertThat(provider.isAvailable()).isTrue();
    }

    @Test
    void configuredPlaceholderCannotReportDeliverySuccessWithoutTransport() {
        var provider = new FcmPushProvider("synthetic-key", "project-one");
        var result = provider.send(new ChannelSendRequest(List.of(TOKEN), "receipt", null, null));
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.isSuccess(TOKEN)).isFalse();
        assertThat(result.successCount()).isZero();
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(result.getErrorMessage(TOKEN)).isNotBlank();
    }

    @Test
    void forwardsContentAndCustomDataToTheProtectedTransportBoundary() {
        var provider = new RecordingProvider();
        var result = provider.send(new ChannelSendRequest(
                List.of(TOKEN, OTHER_TOKEN),
                "Payment received",
                Map.of("extras", Map.of("orderId", "order-1", "amount", "10.00")),
                null));
        assertThat(provider.deliveries)
                .containsExactly(
                        new Delivery(TOKEN, "Payment received", Map.of("orderId", "order-1", "amount", "10.00")),
                        new Delivery(OTHER_TOKEN, "Payment received", Map.of("orderId", "order-1", "amount", "10.00")));
        assertThat(result.successCount()).isEqualTo(2);
        assertThat(result.failedCount()).isZero();
        assertThat(result.isSuccess(TOKEN)).isTrue();
        assertThat(result.receiverErrors()).isEmpty();
    }

    @Test
    void missingAndNonMapExtrasProduceEmptyTransportData() {
        var provider = new RecordingProvider();
        provider.send(new ChannelSendRequest(List.of(TOKEN), "receipt", null, null));
        provider.send(new ChannelSendRequest(List.of(TOKEN), "receipt", Map.of("extras", "invalid"), null));
        assertThat(provider.deliveries)
                .allSatisfy(delivery -> assertThat(delivery.extras()).isEmpty());
        assertThat(provider.deliveries).hasSize(2);
    }

    @Test
    void rejectsShortAndMalformedTokensWithoutCallingTransport() {
        var provider = new RecordingProvider();
        var result = provider.send(new ChannelSendRequest(
                List.of("short", "invalid.token.of.sufficient.length", TOKEN), "receipt", null, null));
        assertThat(provider.deliveries).extracting(Delivery::token).containsExactly(TOKEN);
        assertThat(result.failedCount()).isEqualTo(2);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.getErrorMessage("short")).isEqualTo("invalid device token format");
        assertThat(result.isSuccess("invalid.token.of.sufficient.length")).isFalse();
    }

    @Test
    void failedTransportDoesNotPreventLaterRecipientsFromBeingSent() {
        var provider = new RecordingProvider() {
            @Override
            protected void sendMessage(String token, String content, Map<String, Object> extras) throws Exception {
                if (TOKEN.equals(token)) {
                    throw new Exception("Transport unavailable");
                }
                super.sendMessage(token, content, extras);
            }
        };
        var result = provider.send(new ChannelSendRequest(List.of(TOKEN, OTHER_TOKEN), "receipt", null, null));
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.getErrorMessage(TOKEN)).isEqualTo("Transport unavailable");
        assertThat(result.isSuccess(OTHER_TOKEN)).isTrue();
        assertThat(provider.deliveries).extracting(Delivery::token).containsExactly(OTHER_TOKEN);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void missingCredentialsAreUnavailableAndProduceFailure(String key) {
        var provider = new FcmPushProvider(key, "project-one");
        assertThat(provider.isAvailable()).isFalse();
        var result = provider.send(new ChannelSendRequest(List.of(TOKEN), "receipt", null, null));
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(result.getErrorMessage(TOKEN)).isEqualTo("FCM server key is not configured");
    }

    @Test
    void emptyReceiverListDoesNotCallTransport() {
        var provider = new RecordingProvider();
        var result = provider.send(new ChannelSendRequest(null, "receipt", null, null));
        assertThat(provider.deliveries).isEmpty();
        assertThat(result.receiverStatus()).isEmpty();
        assertThat(result.successCount()).isZero();
        assertThat(result.failedCount()).isZero();
    }

    private record Delivery(String token, String content, Map<String, Object> extras) {}

    private static class RecordingProvider extends FcmPushProvider {
        final List<Delivery> deliveries = new ArrayList<>();

        RecordingProvider() {
            super("synthetic-key", "project-one");
        }

        @Override
        protected void sendMessage(String token, String content, Map<String, Object> extras) throws Exception {
            deliveries.add(new Delivery(token, content, Map.copyOf(extras)));
        }
    }
}
