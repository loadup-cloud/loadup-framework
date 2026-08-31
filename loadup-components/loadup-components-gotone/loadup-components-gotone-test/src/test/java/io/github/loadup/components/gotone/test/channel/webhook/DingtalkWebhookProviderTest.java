/*-
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import io.github.loadup.components.gotone.channel.webhook.provider.DingtalkWebhookProvider;
import io.github.loadup.components.gotone.model.ChannelSendRequest;
import io.github.loadup.components.gotone.model.ChannelSendResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DingtalkWebhookProviderTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postsDingTalkPayloadAndReportsSuccess() throws IOException {
        AtomicReference<String> receivedBody = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/robot", exchange -> {
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), UTF_8));
            byte[] response = "{\"errcode\":0}".getBytes(UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();

        DingtalkWebhookProvider provider = new DingtalkWebhookProvider();
        ChannelSendResponse response = provider.send(new ChannelSendRequest(
                List.of("13800138000"),
                "hello dingtalk",
                Map.of("webhookUrl", "http://localhost:" + server.getAddress().getPort() + "/robot"),
                Map.of()));

        assertThat(response.successCount()).isEqualTo(1);
        assertThat(receivedBody.get()).contains("\"msgtype\":\"text\"");
        assertThat(receivedBody.get()).contains("hello dingtalk");
    }

    @Test
    void reportsFailureForNon2xxResponse() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/robot", exchange -> {
            byte[] response = "{\"errcode\":500}".getBytes(UTF_8);
            exchange.sendResponseHeaders(500, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();

        DingtalkWebhookProvider provider = new DingtalkWebhookProvider();
        ChannelSendResponse response = provider.send(new ChannelSendRequest(
                List.of("13800138000"),
                "hello",
                Map.of("webhookUrl", "http://localhost:" + server.getAddress().getPort() + "/robot"),
                Map.of()));

        assertThat(response.successCount()).isZero();
        assertThat(response.failedCount()).isEqualTo(1);
    }

    @Test
    void missingWebhookUrlFailsFast() {
        DingtalkWebhookProvider provider = new DingtalkWebhookProvider();

        ChannelSendResponse response =
                provider.send(new ChannelSendRequest(List.of("13800138000"), "hello", Map.of(), Map.of()));

        assertThat(response.isSuccess()).isFalse();
    }
}
