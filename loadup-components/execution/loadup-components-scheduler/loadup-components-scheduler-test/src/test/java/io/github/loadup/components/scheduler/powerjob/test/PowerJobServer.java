/*
 * #%L
 * LoadUp Components PowerJob Test
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
package io.github.loadup.components.scheduler.powerjob.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.testcontainers.mysql.MySQLContainer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Runs the published server in a separate JVM with disposable infrastructure. */
final class PowerJobServer implements AutoCloseable {
    private final MySQLContainer mysql = new MySQLContainer("mysql:8.0")
            .withDatabaseName("powerjob")
            .withUsername("powerjob")
            .withPassword(UUID.randomUUID().toString())
            .withEnv("TZ", java.time.ZoneId.systemDefault().getId());
    private final HttpClient http =
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final JsonMapper json = JsonMapper.builder().build();
    private final int port = freePort();
    private final int transportPort = freePort();
    private final String adminPassword = UUID.randomUUID().toString();
    final String appPassword = UUID.randomUUID().toString();
    final String appName = "loadup-poc-" + UUID.randomUUID();
    private final Path directory = Path.of("target/server").toAbsolutePath();
    private Process process;
    private Path configuration;
    private String managementToken;
    private long appId;

    String address() {
        return "127.0.0.1:" + port;
    }

    void start() throws Exception {
        mysql.start();
        Properties properties = new Properties();
        properties.setProperty("server.port", String.valueOf(port));
        properties.setProperty("server.address", "127.0.0.1");
        properties.setProperty("spring.main.banner-mode", "off");
        properties.setProperty("spring.jpa.open-in-view", "false");
        properties.setProperty("spring.data.mongodb.repositories.type", "none");
        properties.setProperty("spring.datasource.core.driver-class-name", "com.mysql.cj.jdbc.Driver");
        properties.setProperty(
                "spring.datasource.core.jdbc-url",
                mysql.getJdbcUrl() + "?serverTimezone="
                        + java.time.ZoneId.systemDefault().getId());
        properties.setProperty("spring.datasource.core.username", mysql.getUsername());
        properties.setProperty("spring.datasource.core.password", mysql.getPassword());
        properties.setProperty("oms.env", "PRODUCT");
        properties.setProperty("oms.instance.metadata.cache.size", "2048");
        properties.setProperty("oms.accurate.select.server.percentage", "50");
        properties.setProperty("oms.instanceinfo.retention", "7");
        properties.setProperty("oms.container.retention.local", "7");
        properties.setProperty("oms.container.retention.remote", "-1");
        properties.setProperty("oms.transporter.active.protocols", "HTTP");
        properties.setProperty("oms.transporter.main.protocol", "HTTP");
        properties.setProperty("oms.http.port", String.valueOf(transportPort));
        properties.setProperty("oms.auth.initiliaze.admin.password", adminPassword);
        properties.setProperty("oms.auth.security.jwt.secret", UUID.randomUUID().toString());
        properties.setProperty("oms.auth.openapi.enable", "false");
        properties.setProperty("logging.level.root", "WARN");
        properties.setProperty("logging.level.tech.powerjob.server.initializer", "ERROR");
        configuration = Files.createTempFile(directory, "server-", ".properties");
        try (var output = Files.newOutputStream(configuration)) {
            properties.store(output, "Disposable PoC credentials");
        }
        launch();
        bootstrapApplication();
    }

    private void launch() throws Exception {
        String java = System.getenv("POWERJOB_SERVER_JAVA");
        if (java == null || java.isBlank())
            java = Path.of(System.getProperty("java.home"), "bin/java").toString();
        process = new ProcessBuilder(
                        java,
                        "-Xmx512m",
                        "-Duser.home=" + directory,
                        "-jar",
                        directory.resolve("server.jar").toString(),
                        "--spring.config.location=" + configuration.toUri())
                .directory(directory.toFile())
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.appendTo(
                        directory.resolve("server.log").toFile()))
                .start();
        await().atMost(Duration.ofMinutes(2))
                .pollInterval(Duration.ofSeconds(1))
                .until(() -> {
                    if (!process.isAlive())
                        throw new IllegalStateException("PowerJob Server exited; inspect target/server/server.log");
                    try {
                        return http.send(
                                                HttpRequest.newBuilder(URI.create(
                                                                "http://" + address() + "/auth/supportLoginTypes"))
                                                        .timeout(Duration.ofSeconds(2))
                                                        .GET()
                                                        .build(),
                                                HttpResponse.BodyHandlers.discarding())
                                        .statusCode()
                                == 200;
                    } catch (IOException e) {
                        return false;
                    }
                });
    }

    private void bootstrapApplication() throws Exception {
        var loginResponse = new java.util.concurrent.atomic.AtomicReference<JsonNode>();
        await().ignoreExceptions()
                .atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> loginResponse.set(post(
                        "/auth/thirdPartyLoginDirect",
                        Map.of(
                                "loginType",
                                "PWJB",
                                "originParams",
                                json.writeValueAsString(Map.of("username", "ADMIN", "password", adminPassword))),
                        null)));
        JsonNode login = loginResponse.get();
        String token = login.path("data").path("jwtToken").asText();
        assertThat(token).isNotBlank();
        JsonNode namespace = post("/namespace/save", Map.of("name", "LoadUp PoC", "code", "loadup-poc"), token);
        JsonNode application = post(
                "/appInfo/save",
                Map.of(
                        "appName",
                        appName,
                        "title",
                        "LoadUp PoC",
                        "namespaceId",
                        namespace.path("data").path("id").asLong(),
                        "password",
                        appPassword),
                token);
        managementToken = token;
        appId = application.path("data").path("id").asLong();
        assertThat(appId).isPositive();
    }

    private JsonNode post(String path, Map<String, ?> body, String token) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://" + address() + path))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json");
        if (token != null) request.header("PowerJwt", token);
        var response = http.send(
                request.POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as(path).isEqualTo(200);
        JsonNode result = json.readTree(response.body());
        assertThat(result.path("success").asBoolean())
                .as("%s success (message=%s)", path, result.path("message").asText())
                .isTrue();
        return result;
    }

    void awaitWorker() {
        await().ignoreExceptions()
                .atMost(Duration.ofSeconds(45))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    var request = HttpRequest.newBuilder(
                                    URI.create("http://" + address() + "/system/listWorker?appId=" + appId))
                            .timeout(Duration.ofSeconds(3))
                            .header("PowerJwt", managementToken)
                            .header("AppId", Long.toString(appId))
                            .GET()
                            .build();
                    var response = http.send(request, HttpResponse.BodyHandlers.ofString());
                    assertThat(response.statusCode()).isEqualTo(200);
                    JsonNode result = json.readTree(response.body());
                    assertThat(result.path("success").asBoolean()).isTrue();
                    assertThat(result.path("data")).anySatisfy(node -> {
                        assertThat(node.path("status").asInt()).isBetween(1, 3);
                        assertThat(node.path("overloading").asBoolean()).isFalse();
                    });
                });
    }

    void restart() throws Exception {
        stopProcess();
        launch();
    }

    private void stopProcess() throws InterruptedException {
        if (process != null && process.isAlive()) {
            process.destroy();
            if (!process.waitFor(15, java.util.concurrent.TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor(10, java.util.concurrent.TimeUnit.SECONDS);
            }
        }
    }

    static int freePort() {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot allocate PoC port", e);
        }
    }

    @Override
    public void close() throws Exception {
        try {
            stopProcess();
        } finally {
            try {
                mysql.stop();
            } finally {
                if (configuration != null) Files.deleteIfExists(configuration);
                http.close();
            }
        }
    }
}
