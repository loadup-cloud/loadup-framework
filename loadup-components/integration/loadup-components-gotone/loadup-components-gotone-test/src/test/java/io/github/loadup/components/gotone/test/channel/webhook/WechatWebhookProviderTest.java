package io.github.loadup.components.gotone.test.channel.webhook;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import io.github.loadup.components.gotone.channel.webhook.provider.WechatWebhookProvider;
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

class WechatWebhookProviderTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postsWeChatPayloadAndReportsSuccess() throws IOException {
        AtomicReference<String> receivedBody = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/webhook", exchange -> {
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), UTF_8));
            byte[] response = "{\"errcode\":0}".getBytes(UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();

        WechatWebhookProvider provider = new WechatWebhookProvider();
        ChannelSendResponse response = provider.send(new ChannelSendRequest(
                List.of("13800138000"),
                "hello wechat",
                Map.of("webhookUrl", "http://localhost:" + server.getAddress().getPort() + "/webhook"),
                Map.of()));

        assertThat(response.successCount()).isEqualTo(1);
        assertThat(receivedBody.get()).contains("\"msgtype\":\"text\"");
        assertThat(receivedBody.get()).contains("hello wechat");
        assertThat(receivedBody.get()).contains("13800138000");
    }
}
