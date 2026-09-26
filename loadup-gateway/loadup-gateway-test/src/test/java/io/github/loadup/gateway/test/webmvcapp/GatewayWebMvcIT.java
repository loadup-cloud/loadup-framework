package io.github.loadup.gateway.test.webmvcapp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import io.github.loadup.gateway.api.event.RouteSourceChangedEvent;
import io.github.loadup.gateway.api.model.ManagedRoute;
import io.github.loadup.gateway.api.model.RouteDocument;
import io.github.loadup.gateway.api.spi.RouteSource;
import io.github.loadup.gateway.plugins.yaml.YamlRouteSource;
import io.github.loadup.gateway.webmvc.managed.GatewayRoutesEndpoint;
import io.github.loadup.gateway.webmvc.managed.ManagedRouteRegistry;
import io.github.loadup.gateway.webmvc.managed.ServiceMethodCatalog;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = GatewayWebMvcTestApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "loadup.gateway.source.file.path=classpath:gateway-routes.yml",
            "spring.aop.proxy-target-class=false"
        })
class GatewayWebMvcIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ManagedRouteRegistry registry;

    @Autowired
    private ServiceMethodCatalog catalog;

    @Autowired
    private ApplicationEventPublisher events;

    @Autowired
    private DemoContract contract;

    @Test
    void invokesOnlyExposedServiceMethods() throws Exception {
        mockMvc.perform(post("/api/demo/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"loadup\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("\"echo:loadup\""));
        org.junit.jupiter.api.Assertions.assertEquals(
                1, registry.currentDocument().schemaVersion());
    }

    @Test
    void authenticatesAndAuthorizesManagedRoutes() throws Exception {
        mockMvc.perform(post("/api/demo/secure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/demo/authorized")
                        .header("Authorization", "Bearer " + signedToken(List.of("user:read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/demo/authorized")
                        .header("Authorization", "Bearer " + signedToken(List.of("user:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"loadup\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void invokesMethodSecurityThroughSpringProxy() throws Exception {
        mockMvc.perform(post("/api/demo/protected")
                        .header("Authorization", "Bearer " + signedToken(List.of("user:write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"loadup\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("\"secure:loadup\""));
    }

    @Test
    void findsInterfaceExposureAndInvokesJdkProxy() throws Exception {
        org.junit.jupiter.api.Assertions.assertTrue(AopUtils.isJdkDynamicProxy(contract));
        mockMvc.perform(get("/api/demo/contract/42"))
                .andExpect(status().isOk())
                .andExpect(content().string("\"contract:42\""));
    }

    @Test
    void limitsRequestsOnManagedRoute() throws Exception {
        mockMvc.perform(get("/api/demo/limited/42")).andExpect(status().isOk());
        mockMvc.perform(get("/api/demo/limited/42")).andExpect(status().isTooManyRequests());
    }

    @Test
    void verifiesBodyAndRejectsSignatureReplay() throws Exception {
        String path = "/api/demo/signed";
        String body = "{\"name\":\"signed\"}";
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String nonce = "signed-request-1";
        String signature = sign("POST", path, body, timestamp, nonce);
        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-App-Id", "demo-app")
                        .header("X-Timestamp", timestamp)
                        .header("X-Nonce", nonce)
                        .header("X-Signature", signature)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("\"echo:signed\""));
        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-App-Id", "demo-app")
                        .header("X-Timestamp", timestamp)
                        .header("X-Nonce", nonce)
                        .header("X-Signature", signature)
                        .content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-App-Id", "demo-app")
                        .header("X-Timestamp", timestamp)
                        .header("X-Nonce", "signed-request-2")
                        .header("X-Signature", sign("POST", path, body, timestamp, "signed-request-2"))
                        .content("{\"name\":\"tampered\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void combinesJwtAndSignatureWithAndSemantics() throws Exception {
        String path = "/api/demo/signed-auth";
        String body = "{\"name\":\"both\"}";
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String nonce = "signed-auth-1";
        String signature = sign("POST", path, body, timestamp, nonce);
        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-App-Id", "demo-app")
                        .header("X-Timestamp", timestamp)
                        .header("X-Nonce", nonce)
                        .header("X-Signature", signature)
                        .content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(path)
                        .header("Authorization", "Bearer " + signedToken(List.of("user:read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-App-Id", "demo-app")
                        .header("X-Timestamp", timestamp)
                        .header("X-Nonce", nonce)
                        .header("X-Signature", signature)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().string("\"echo:both\""));
    }

    @Test
    void malformedJsonReturnsBadRequestWithRequestId() throws Exception {
        mockMvc.perform(post("/api/demo/echo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Request-Id"));
    }

    @Test
    void doesNotMatchUnknownPaths() throws Exception {
        mockMvc.perform(post("/api/unknown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidRevisionKeepsPreviousRoutingSnapshot() {
        ManagedRoute valid = registry.currentDocument().routes().getFirst();
        AtomicReference<RouteDocument> sourceValue =
                new AtomicReference<>(new RouteDocument("test", "r1", 1, List.of(valid)));
        RouteSource source = sourceValue::get;
        ManagedRouteRegistry local = new ManagedRouteRegistry(source, catalog);
        local.init();
        ManagedRoute invalid = new ManagedRoute(
                valid.id(),
                valid.order(),
                valid.path(),
                valid.methods(),
                new ManagedRoute.Target("service", "demoEchoService", "notExposed", null),
                valid.access(),
                valid.filters());
        sourceValue.set(new RouteDocument("test", "r2", 1, List.of(invalid)));
        local.refresh();
        org.junit.jupiter.api.Assertions.assertEquals(
                "r1", local.currentDocument().revision());
        org.junit.jupiter.api.Assertions.assertNotNull(local.lastError());
        GatewayRoutesEndpoint.Status status = new GatewayRoutesEndpoint(local).status();
        org.junit.jupiter.api.Assertions.assertEquals("failure", status.lastRefreshOutcome());
        org.junit.jupiter.api.Assertions.assertEquals("r1", status.revision());
        sourceValue.set(new RouteDocument("test", "r3", 1, List.of(valid)));
        local.refresh();
        org.junit.jupiter.api.Assertions.assertEquals(
                "r3", local.currentDocument().revision());
    }

    @Test
    void rejectsIdAlreadyOwnedByStaticScgRoute() {
        ManagedRoute route = registry.currentDocument().routes().getFirst();
        RouteSource source = () -> new RouteDocument("test", "conflict", 1, List.of(route));
        ManagedRouteRegistry local = new ManagedRouteRegistry(source, catalog, Set.of(route.id()));
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, local::init);
    }

    @Test
    void externalFileCanReplaceRoutesWithoutRestart(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("gateway-routes.yml");
        Files.writeString(file, fileDocument("/api/file/one", "echo"));
        YamlRouteSource source = new YamlRouteSource(file.toString(), 1, events);
        ManagedRouteRegistry local = new ManagedRouteRegistry(source, catalog);
        local.init();
        String firstRevision = local.currentDocument().revision();
        Files.writeString(file, fileDocument("/api/file/broken", "notExposed"));
        local.refresh();
        org.junit.jupiter.api.Assertions.assertEquals(
                firstRevision, local.currentDocument().revision());
        Files.writeString(file, fileDocument("/api/file/two", "echo"));
        local.refresh();
        org.junit.jupiter.api.Assertions.assertEquals(
                "/api/file/two", local.currentDocument().routes().getFirst().path());
        org.junit.jupiter.api.Assertions.assertNotEquals(
                firstRevision, local.currentDocument().revision());
    }

    @Test
    void externalFileChangePublishesWithoutManualRefresh(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("gateway-routes.yml");
        Files.writeString(file, fileDocument("/api/file/before", "echo"));
        AtomicReference<ManagedRouteRegistry> current = new AtomicReference<>();
        ApplicationEventPublisher publisher = event -> {
            if (event instanceof RouteSourceChangedEvent changed && current.get() != null) {
                current.get().sourceChanged(changed);
            }
        };
        YamlRouteSource source = new YamlRouteSource(file.toString(), 1, publisher);
        ManagedRouteRegistry local = new ManagedRouteRegistry(source, catalog);
        current.set(local);
        local.init();
        source.start();
        try {
            Files.writeString(file, fileDocument("/api/file/after", "echo"));
            long deadline = System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(5);
            while (!"/api/file/after"
                            .equals(local.currentDocument().routes().getFirst().path())
                    && System.nanoTime() < deadline) {
                Thread.sleep(50);
            }
            org.junit.jupiter.api.Assertions.assertEquals(
                    "/api/file/after",
                    local.currentDocument().routes().getFirst().path());
        } finally {
            source.stop();
        }
    }

    @Test
    void rejectsUnknownRouteFieldsBeforePublication() {
        String document =
                fileDocument("/api/file/one", "echo").replace("    order: 10", "    order: 10\n    enabld: true");
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> YamlRouteSource.parse("test", document.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void rejectsNonIntegerSchemaVersion() {
        String document = fileDocument("/api/file/one", "echo").replace("schemaVersion: 1", "schemaVersion: 1.5");
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> YamlRouteSource.parse("test", document.getBytes(StandardCharsets.UTF_8)));
    }

    private static String fileDocument(String path, String method) {
        return "schemaVersion: 1\nroutes:\n  - id: file-route\n    order: 10\n    path: " + path
                + "\n    methods: [POST]\n    target: { type: service, bean: demoEchoService, method: "
                + method + " }\n    access: { type: public }\n";
    }

    private static String signedToken(List<String> permissions) {
        String secret = "loadup-test-only-gateway-key-0123456789abcdef";
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("loadup")
                .subject("u-1")
                .claim("username", "admin")
                .claim("roles", List.of("ADMIN"))
                .claim("permissions", permissions)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private static String sign(String method, String path, String body, String timestamp, String nonce)
            throws Exception {
        String hash = HexFormat.of()
                .formatHex(MessageDigest.getInstance("SHA-256").digest(body.getBytes(StandardCharsets.UTF_8)));
        String canonical = method + "\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + hash;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("loadup-signature-test-secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
    }
}
