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
package io.github.loadup.components.gotone.test.channel.sms;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.channel.sms.AliyunSmsProvider;
import io.github.loadup.components.gotone.channel.sms.HuaweiSmsProvider;
import io.github.loadup.components.gotone.channel.sms.YunpianSmsProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class SmsProviderContractTest {
    static Stream<NotificationChannelProvider> missingCredentials() {
        List<NotificationChannelProvider> providers = new ArrayList<>();
        for (String[] pair : new String[][] {{null, "secret"}, {" ", "secret"}, {"key", null}, {"key", " "}}) {
            providers.add(new AliyunSmsProvider(pair[0], pair[1], "sign", "region"));
            providers.add(new HuaweiSmsProvider(pair[0], pair[1], "sender", "sign", "endpoint"));
        }
        providers.add(new YunpianSmsProvider(null, "endpoint"));
        providers.add(new YunpianSmsProvider(" ", "endpoint"));
        return providers.stream();
    }

    static Stream<NotificationChannelProvider> configuredPlaceholders() {
        return Stream.of(
                new AliyunSmsProvider("key", "secret", "sign", "region"),
                new HuaweiSmsProvider("key", "secret", "sender", "sign", "endpoint"),
                new YunpianSmsProvider("key", "endpoint"));
    }

    private static void capture(
            List<List<String>> captured, String receiver, String template, String sign, String content) {
        if (receiver.equals("+19999999999")) {
            throw new IllegalStateException("synthetic transport failure");
        }
        captured.add(List.of(receiver, template, sign, content));
    }

    @ParameterizedTest
    @MethodSource("missingCredentials")
    void missingCredentialsProduceFailures(NotificationChannelProvider provider) {
        assertThat(provider.isAvailable()).isFalse();
        var result = provider.send(new ChannelSendRequest(List.of("+8613800000000"), "test", null, null));
        assertThat(result.successCount()).isZero();
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(result.isSuccess("+8613800000000")).isFalse();
        assertThat(result.getErrorMessage("+8613800000000")).isNotBlank();
    }

    @ParameterizedTest
    @MethodSource("configuredPlaceholders")
    void credentialsDoNotTurnAnUnimplementedTransportIntoDeliverySuccess(NotificationChannelProvider provider) {
        assertThat(provider.isAvailable()).isTrue();
        assertThat(provider.getChannelType()).isEqualTo("SMS");
        assertThat(provider.getProviderName()).isIn("aliyun", "huawei", "yunpian");
        var result = provider.send(new ChannelSendRequest(List.of("+8613800000000"), "test", null, null));
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.failedCount()).isEqualTo(1);
    }

    @Test
    void templatesAndVendorSpecificOverridesReachTheTransportAndErrorsArePerReceiver() {
        List<List<String>> captured = new ArrayList<>();
        List<NotificationChannelProvider> providers = List.of(
                new AliyunSmsProvider("key", "secret", "default-sign", "region") {
                    protected void sendMessage(String receiver, String template, String sign, String content) {
                        capture(captured, receiver, template, sign, content);
                    }
                },
                new HuaweiSmsProvider("key", "secret", "default-sender", "default-sign", "endpoint") {
                    protected void sendMessage(
                            String receiver, String template, String sign, String sender, String content) {
                        capture(captured, receiver, template, sign, content);
                        assertThat(sender).isEqualTo("override-sender");
                    }
                },
                new YunpianSmsProvider("key", "endpoint") {
                    protected void sendMessage(String receiver, String template, String content) {
                        capture(captured, receiver, template, "override-sign", content);
                    }
                });
        var request = new ChannelSendRequest(
                List.of("bad", "invalid-number", "+12345", "+8613800000000", "+19999999999"),
                "rendered-content",
                Map.of(
                        "templateId",
                        42,
                        "signName",
                        "override-sign",
                        "signature",
                        "override-sign",
                        "sender",
                        "override-sender"),
                null);
        for (var provider : providers) {
            var result = provider.send(request);
            assertThat(result.successCount()).isEqualTo(2);
            assertThat(result.failedCount()).isEqualTo(3);
            assertThat(result.receiverStatus()).containsEntry("+12345", true).containsEntry("bad", false);
            assertThat(result.getErrorMessage("+19999999999")).isEqualTo("synthetic transport failure");
            assertThat(provider.send(new ChannelSendRequest(List.of(), "empty", null, null))
                            .receiverStatus())
                    .isEmpty();
        }
        assertThat(captured).hasSize(6);
        assertThat(captured)
                .allSatisfy(values ->
                        assertThat(values.subList(1, 4)).containsExactly("42", "override-sign", "rendered-content"));
    }
}
