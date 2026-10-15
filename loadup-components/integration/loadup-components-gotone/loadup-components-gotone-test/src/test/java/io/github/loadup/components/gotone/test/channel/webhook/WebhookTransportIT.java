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
package io.github.loadup.components.gotone.test.channel.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.channel.webhook.provider.DingtalkWebhookProvider;
import io.github.loadup.components.gotone.channel.webhook.provider.FeishuWebhookProvider;
import io.github.loadup.components.gotone.channel.webhook.provider.WechatWebhookProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/** Real loopback HTTP transport contracts; no vendor endpoint is contacted. */
class WebhookTransportIT {
    private HttpServer server;
    private final AtomicInteger status = new AtomicInteger(200);
    private final AtomicReference<String> method = new AtomicReference<>();
    private final AtomicReference<String> contentType = new AtomicReference<>();
    private final AtomicReference<String> body = new AtomicReference<>();
    private final JsonMapper json = JsonMapper.builder().build();

    @AfterEach
    void closeServer() {
        Thread.interrupted();
        if (server != null) server.stop(0);
    }

    private String endpoint() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/webhook", exchange -> {
            method.set(exchange.getRequestMethod());
            contentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            body.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            exchange.sendResponseHeaders(status.get(), -1);
            exchange.close();
        });
        server.start();
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/webhook";
    }

    private List<NotificationChannelProvider> providers() {
        return List.of(new WechatWebhookProvider(), new DingtalkWebhookProvider(), new FeishuWebhookProvider());
    }

    @Test
    void allProvidersUseJsonPostAndRejectNonSuccessfulHttpStatuses() throws Exception {
        String url = endpoint();
        for (var provider : providers()) {
            assertThat(provider.getChannelType()).isEqualTo("WEBHOOK");
            assertThat(provider.isAvailable()).isTrue();
            status.set(200);
            var result = provider.send(
                    new ChannelSendRequest(List.of("+1234567"), "hello", Map.of("webhookUrl", url), null));
            assertThat(result.successCount()).isEqualTo(1);
            assertThat(method.get()).isEqualTo("POST");
            assertThat(contentType.get()).isEqualTo("application/json");
            assertThat(result.receiverStatus()).containsEntry(provider.getProviderName(), true);
            assertThat(json.readTree(body.get()).toString()).contains("hello");
            for (int failureStatus : new int[] {302, 500}) {
                status.set(failureStatus);
                var failed = provider.send(new ChannelSendRequest(List.of(), "hello", Map.of("webhookUrl", url), null));
                assertThat(failed.failedCount()).isEqualTo(1);
                assertThat(failed.receiverErrors()).containsKey(provider.getProviderName());
            }
        }
    }

    @Test
    void mentionedUsersAndMarkdownOptionsReachTheirVendorPayloads() throws Exception {
        String url = endpoint();
        var wechat = new WechatWebhookProvider();
        wechat.send(new ChannelSendRequest(
                List.of("default"),
                "hello",
                Map.of("webhookUrl", url, "mentionedList", List.of("alice"), "mentionedMobileList", List.of("ignored")),
                null));
        assertThat(json.readTree(body.get())
                        .path("text")
                        .path("mentioned_list")
                        .get(0)
                        .asString())
                .isEqualTo("alice");
        wechat.send(new ChannelSendRequest(
                List.of("default"),
                "hello",
                Map.of("webhookUrl", url, "mentionedMobileList", List.of("override")),
                null));
        assertThat(json.readTree(body.get())
                        .path("text")
                        .path("mentioned_mobile_list")
                        .get(0)
                        .asString())
                .isEqualTo("override");
        new DingtalkWebhookProvider()
                .send(new ChannelSendRequest(
                        List.of("12345"),
                        "**hello**",
                        Map.of("webhookUrl", url, "msgtype", "markdown", "title", "incident", "atAll", true),
                        null));
        var payload = json.readTree(body.get());
        assertThat(payload.path("markdown").path("title").asString()).isEqualTo("incident");
        assertThat(payload.path("markdown").path("text").asString()).isEqualTo("**hello**");
        assertThat(payload.path("at").path("isAtAll").asBoolean()).isTrue();
    }

    @Test
    void absentBlankAndMalformedUrlsReturnFailures() {
        for (var provider : providers()) {
            for (Map<String, Object> config : List.<Map<String, Object>>of(
                    Map.of(), Map.of("webhookUrl", " "), Map.of("webhookUrl", "invalid uri"))) {
                var result = provider.send(new ChannelSendRequest(List.of(), "hello", config, null));
                assertThat(result.isSuccess()).isFalse();
                assertThat(result.receiverErrors().get(provider.getProviderName()))
                        .isNotBlank();
            }
        }
    }

    @Test
    void interruptedRequestsReturnFailureAndPreserveCancellation() throws Exception {
        String url = endpoint();
        for (var provider : providers()) {
            Thread.currentThread().interrupt();
            try {
                var result = provider.send(new ChannelSendRequest(List.of(), "hello", Map.of("webhookUrl", url), null));
                assertThat(result.isSuccess()).isFalse();
                assertThat(Thread.currentThread().isInterrupted()).isTrue();
                assertThat(result.receiverErrors()).containsEntry(provider.getProviderName(), "webhook request failed");
            } finally {
                Thread.interrupted();
            }
        }
    }
}
