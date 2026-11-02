package io.github.loadup.components.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;

class HttpServiceClientsTest {
    private HttpServer server;
    private HttpTemplate http;
    private final AtomicInteger calls = new AtomicInteger();
    private final AtomicReference<String> authorization = new AtomicReference<>();

    public record Value(String value) {
    }

    public record Envelope<T>(T data) {
    }

    public record Integral(long value) {}

    public interface Service {
        @PostExchange(url = "/echo", contentType = "application/json", accept = "application/json")
        Envelope<List<Value>> echo(@RequestHeader("Authorization") String token, @RequestBody Value body);

        @GetExchange("/fail")
        Value fail();

        @GetExchange("/redirect")
        Value redirect();

        @GetExchange("/slow")
        Value slow();

        @GetExchange("/chunked")
        Value chunked();

        @GetExchange("/invalid-json")
        Value invalidJson();

        @GetExchange("https://other.invalid/echo")
        Value otherOrigin();

        @GetExchange("/fractional")
        Integral fractional();
    }

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/echo", exchange -> {
            calls.incrementAndGet();
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] request = exchange.getRequestBody().readAllBytes();
            byte[] response = ("{\"data\":[" + new String(request, StandardCharsets.UTF_8) + "]}")
                .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.createContext("/fail", exchange -> {
            calls.incrementAndGet();
            byte[] body = "sensitive remote detail".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(503, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/redirect", exchange -> {
            calls.incrementAndGet();
            exchange.getResponseHeaders().set("Location", "/echo");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        server.createContext("/chunked", exchange -> {
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(new byte[256]);
            exchange.close();
        });
        server.createContext("/invalid-json", exchange -> {
            byte[] body = "sensitive invalid JSON".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/fractional", exchange -> {
            byte[] body = "{\"value\":1.5}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(200);
                exchange.sendResponseHeaders(200, -1);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();
        http = new HttpTemplate(config(1024, 1024, true, Duration.ofSeconds(1)),
            RestClient::builder, JsonMapper.builder()
                    .disable(tools.jackson.databind.DeserializationFeature.ACCEPT_FLOAT_AS_INT).build(), null, null, null);
    }

    @Test
    void declarativeJsonUsesTheSameInjectedMapperAsNamedOperations() {
        var client = http.serviceClient("target", Service.class);
        assertThatThrownBy(client::fractional).isInstanceOf(HttpCallException.class);
    }

    private HttpProperties config(int requestLimit, long responseLimit, boolean privateAddresses, Duration readTimeout) {
        return new HttpProperties(Map.of("target", new HttpProperties.Client(
            URI.create("http://127.0.0.1:" + server.getAddress().getPort()),
            Duration.ofSeconds(1), readTimeout, Duration.ofSeconds(1), 2, 2,
            requestLimit, responseLimit, null, null, privateAddresses, null, Map.of(), Map.of())));
    }

    @AfterEach
    void stop() {
        if (http != null) {
            http.close();
        }
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void decodesGenericJsonAndUsesTheCurrentTokenWithoutOperationUrls() {
        var client = http.serviceClient("target", Service.class);
        assertThat(client.echo("Bearer first", new Value("one")).data()).containsExactly(new Value("one"));
        assertThat(authorization.get()).isEqualTo("Bearer first");
        client.echo("Bearer rotated", new Value("two"));
        assertThat(authorization.get()).isEqualTo("Bearer rotated");
    }

    @Test
    void failuresAndRedirectsAreSafeAndNeverAutomaticallyRetried() {
        var client = http.serviceClient("target", Service.class);
        assertThatThrownBy(client::fail).isInstanceOf(HttpCallException.class)
            .hasMessageNotContaining("sensitive remote detail");
        assertThat(calls.get()).isEqualTo(1);
        assertThatThrownBy(client::redirect).isInstanceOf(HttpCallException.class);
        assertThat(calls.get()).isEqualTo(2);
        assertThatThrownBy(client::otherOrigin).isInstanceOf(IllegalArgumentException.class);
        assertThat(calls.get()).isEqualTo(2);
        assertThatThrownBy(client::invalidJson).isInstanceOf(HttpCallException.class)
                .hasMessageNotContaining("sensitive invalid JSON");
    }

    @Test
    void existingProxyFollowsRefreshAndEnforcesBothSizeLimits() {
        var client = http.serviceClient("target", Service.class);
        client.echo("Bearer first", new Value("one"));
        http.refresh(config(8, 1024, true, Duration.ofSeconds(1)));
        assertThatThrownBy(() -> client.echo("Bearer second", new Value("too large")))
            .isInstanceOf(HttpCallException.class);
        assertThat(calls.get()).isEqualTo(1);
        http.refresh(config(1024, 8, true, Duration.ofSeconds(1)));
        assertThatThrownBy(() -> client.echo("Bearer second", new Value("one")))
            .isInstanceOf(HttpCallException.class);
        assertThatThrownBy(client::chunked).isInstanceOf(HttpCallException.class);
        http.refresh(config(1024, 1024, true, Duration.ofSeconds(1)));
        assertThat(client.echo("Bearer third", new Value("three")).data()).containsExactly(new Value("three"));
    }

    @Test
    void configuredTimeoutPrivateAddressPolicyAndShutdownApplyToServices() {
        var client = http.serviceClient("target", Service.class);
        http.refresh(config(1024, 1024, true, Duration.ofMillis(20)));
        assertThatThrownBy(client::slow).isInstanceOf(HttpCallException.class);
        http.refresh(config(1024, 1024, false, Duration.ofSeconds(1)));
        assertThatThrownBy(() -> client.echo("Bearer token", new Value("one")))
            .isInstanceOf(HttpCallException.class);
        http.close();
        assertThatThrownBy(() -> client.echo("Bearer token", new Value("one")))
            .isInstanceOf(IllegalStateException.class);
    }
}
