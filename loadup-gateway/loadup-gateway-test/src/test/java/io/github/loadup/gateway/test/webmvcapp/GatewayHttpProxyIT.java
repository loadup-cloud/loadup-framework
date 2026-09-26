package io.github.loadup.gateway.test.webmvcapp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sun.net.httpserver.HttpServer;
import io.github.loadup.gateway.api.model.ManagedRoute;
import io.github.loadup.gateway.api.model.RouteDocument;
import io.github.loadup.gateway.api.spi.RouteSource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = GatewayHttpProxyIT.TestApp.class)
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "loadup.gateway.security.enabled=false",
            "loadup.gateway.security.app-secrets.demo-app=loadup-signature-test-secret"
        })
class GatewayHttpProxyIT {
    private static final HttpServer UPSTREAM = upstream();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CircuitBreakerFactory<?, ?> circuitBreakers;

    @Test
    void forwardsHttpThroughScgHandler() throws Exception {
        mockMvc.perform(post("/api/proxy/echo?x=1")
                        .header("X-Test", "forwarded")
                        .contentType("text/plain")
                        .content("payload"))
                .andExpect(status().isMultiStatus())
                .andExpect(header().string("X-Upstream", "yes"))
                .andExpect(content().string("POST /echo?x=1 payload forwarded"));
        Mockito.verify(circuitBreakers, Mockito.atLeastOnce()).create("test-http");
    }

    @Test
    void preservesBinaryUpstreamResponse() throws Exception {
        org.junit.jupiter.api.Assertions.assertArrayEquals(
                new byte[] {0, 1, 2, (byte) 255},
                mockMvc.perform(get("/api/proxy/binary"))
                        .andExpect(status().isOk())
                        .andExpect(header().string("Content-Type", "application/octet-stream"))
                        .andReturn()
                        .getResponse()
                        .getContentAsByteArray());
    }

    @Test
    void appliesNativeRewriteAndRequestHeaderFilters() throws Exception {
        mockMvc.perform(get("/api/rewrite/echo?x=2").header("X-Test", "original"))
                .andExpect(status().isMultiStatus())
                .andExpect(content().string("GET /echo?x=2  managed"));
    }

    @Test
    void forwardsVerifiedSignedBody() throws Exception {
        String path = "/api/proxy/signed/echo";
        String body = "signed payload";
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String nonce = "http-signed-1";
        mockMvc.perform(post(path)
                        .header("X-App-Id", "demo-app")
                        .header("X-Timestamp", timestamp)
                        .header("X-Nonce", nonce)
                        .header("X-Signature", sign(path, body, timestamp, nonce))
                        .contentType("text/plain")
                        .content(body))
                .andExpect(status().isMultiStatus())
                .andExpect(header().string("X-Signature-Seen", "no"))
                .andExpect(content().string("POST /echo signed payload null"));
    }

    @AfterAll
    static void stopUpstream() {
        UPSTREAM.stop(0);
    }

    private static HttpServer upstream() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                if ("/binary".equals(exchange.getRequestURI().getPath())) {
                    byte[] binary = {0, 1, 2, (byte) 255};
                    exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
                    exchange.sendResponseHeaders(200, binary.length);
                    exchange.getResponseBody().write(binary);
                    exchange.close();
                    return;
                }
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String result = exchange.getRequestMethod() + " " + exchange.getRequestURI() + " " + body + " "
                        + exchange.getRequestHeaders().getFirst("X-Test");
                byte[] bytes = result.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("X-Upstream", "yes");
                exchange.getResponseHeaders()
                        .add(
                                "X-Signature-Seen",
                                exchange.getRequestHeaders().getFirst("X-Signature") == null ? "no" : "yes");
                exchange.sendResponseHeaders(207, bytes.length);
                exchange.getResponseBody().write(bytes);
                exchange.close();
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String sign(String path, String body, String timestamp, String nonce) throws Exception {
        String hash = HexFormat.of()
                .formatHex(MessageDigest.getInstance("SHA-256").digest(body.getBytes(StandardCharsets.UTF_8)));
        String canonical = "POST\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + hash;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("loadup-signature-test-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApp {
        @Bean
        RouteSource routes() {
            ManagedRoute signed = new ManagedRoute(
                    "http-signed",
                    5,
                    "/api/proxy/signed/**",
                    List.of("POST"),
                    new ManagedRoute.Target(
                            "http",
                            null,
                            null,
                            "http://127.0.0.1:" + UPSTREAM.getAddress().getPort()),
                    new ManagedRoute.Access("public", List.of(), true),
                    List.of(new ManagedRoute.Filter("StripPrefix", Map.of("parts", "3"))));
            ManagedRoute route = new ManagedRoute(
                    "http-proxy",
                    10,
                    "/api/proxy/**",
                    List.of("GET", "POST"),
                    new ManagedRoute.Target(
                            "http",
                            null,
                            null,
                            "http://127.0.0.1:" + UPSTREAM.getAddress().getPort()),
                    new ManagedRoute.Access("public", List.of()),
                    List.of(
                            new ManagedRoute.Filter("StripPrefix", Map.of("parts", "2")),
                            new ManagedRoute.Filter("CircuitBreaker", Map.of("name", "test-http"))));
            ManagedRoute rewritten = new ManagedRoute(
                    "http-rewrite",
                    15,
                    "/api/rewrite/**",
                    List.of("GET"),
                    new ManagedRoute.Target(
                            "http",
                            null,
                            null,
                            "http://127.0.0.1:" + UPSTREAM.getAddress().getPort()),
                    new ManagedRoute.Access("public", List.of()),
                    List.of(
                            new ManagedRoute.Filter(
                                    "RewritePath",
                                    Map.of("regexp", "/api/rewrite/(?<segment>.*)", "replacement", "/${segment}")),
                            new ManagedRoute.Filter("SetRequestHeader", Map.of("name", "X-Test", "value", "managed"))));
            return () -> new RouteDocument("test", "http-v1", 1, List.of(signed, route, rewritten));
        }

        @Bean
        @SuppressWarnings({"rawtypes", "unchecked"})
        CircuitBreakerFactory<?, ?> circuitBreakerFactory() {
            CircuitBreakerFactory factory = Mockito.mock(CircuitBreakerFactory.class);
            CircuitBreaker breaker = Mockito.mock(CircuitBreaker.class);
            Mockito.doAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get())
                    .when(breaker)
                    .run(Mockito.any(), Mockito.any());
            Mockito.when(factory.create("test-http")).thenReturn(breaker);
            return factory;
        }
    }
}
