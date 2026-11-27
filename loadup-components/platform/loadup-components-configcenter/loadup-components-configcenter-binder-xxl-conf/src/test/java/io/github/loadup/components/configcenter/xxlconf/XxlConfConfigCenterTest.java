/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.configcenter.xxlconf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.ConfigCenterTemplate;
import io.github.loadup.components.configcenter.DynamicConfig;
import io.github.loadup.components.configcenter.autoconfigure.ConfigCenterAutoConfiguration;
import io.github.loadup.components.configcenter.xxlconf.autoconfigure.XxlConfConfigCenterAutoConfiguration;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class XxlConfConfigCenterTest {
    private final AtomicReference<String> value = new AtomicReference<>("true");
    private final AtomicBoolean queryFailure = new AtomicBoolean();
    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/xxl-conf/openapi/confdata/", this::respond);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void templateAndAnnotationUseOpenApiForInitialValuesChangesAndDeletion() {
        runner().withBean(FeatureFlags.class).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(ConfigCenterTemplate.class);
            var template = context.getBean(ConfigCenterTemplate.class);
            var flags = context.getBean(FeatureFlags.class);
            assertThat(flags.enabled).isTrue();
            assertThat(flags.events).containsExactly("true");
            assertThat(template.getConfig("feature.enabled")).isEqualTo("true");
            assertThat(template.listKeys("feature.")).containsExactly("feature.enabled");
            assertThat(template.setConfig("feature.enabled", "false")).isFalse();
            assertThat(template.removeConfig("feature.enabled")).isFalse();

            List<String> explicit = new CopyOnWriteArrayList<>();
            template.addListener("feature.enabled", explicit::add);
            value.set("false");
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                assertThat(flags.enabled).isFalse();
                assertThat(explicit).containsExactly("false");
            });

            template.removeListener("feature.enabled");
            value.set(null);
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
                assertThat(flags.enabled).isTrue();
                assertThat(flags.events).containsExactly("true", "false", "");
            });
            assertThat(explicit).containsExactly("false");
            assertThat(template.getConfig("feature.enabled", "missing")).isEqualTo("missing");

            queryFailure.set(true);
            assertThatThrownBy(() -> template.getConfig("feature.enabled", "missing"))
                    .isInstanceOf(IllegalStateException.class);
        });
    }

    @Test
    void binderRequiresExplicitSelection() {
        runner().withPropertyValues("loadup.configcenter.binder-type=nacos")
                .run(context -> assertThat(context)
                        .doesNotHaveBean(ConfigCenterProvider.class)
                        .doesNotHaveBean(ConfigCenterTemplate.class));
    }

    private ApplicationContextRunner runner() {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        XxlConfConfigCenterAutoConfiguration.class, ConfigCenterAutoConfiguration.class))
                .withPropertyValues(
                        "loadup.configcenter.binder-type=xxl-conf",
                        "loadup.configcenter.binder.xxl-conf.address=http://127.0.0.1:"
                                + server.getAddress().getPort()
                                + "/xxl-conf/",
                        "loadup.configcenter.binder.xxl-conf.access-token=test-token",
                        "loadup.configcenter.binder.xxl-conf.app-name=payments",
                        "loadup.configcenter.binder.xxl-conf.env=test",
                        "feature.key=feature.enabled");
    }

    private void respond(HttpExchange exchange) throws IOException {
        try (exchange) {
            String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (!"POST".equals(exchange.getRequestMethod())
                    || !"test-token".equals(exchange.getRequestHeaders().getFirst("XXL_CONF_ACCESS_TOKEN"))
                    || !request.contains("\"env\":\"test\"")
                    || !request.contains("\"payments\"")) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            String operation = exchange.getRequestURI().getPath().substring("/xxl-conf/openapi/confdata/".length());
            String current = value.get();
            String response =
                    switch (operation) {
                        case "queryKey" -> """
                        {"code":200,"success":true,"data":{"appnameKeyData":{"payments":["unrelated","feature.enabled"]}}}
                        """;
                        case "queryData" -> configuration(request, current);
                        case "monitor" -> """
                        {"code":500,"msg":"Monitor key update: env=test, appname=payments, key=feature.enabled"}
                        """;
                        default -> throw new IllegalArgumentException(operation);
                    };
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
        }
    }

    private String configuration(String request, String current) {
        if (queryFailure.get()
                || !request.contains("\"appnameKeyData\"")
                || !request.contains("\"feature.enabled\"")
                || !request.contains("\"simpleQuery\":false")) {
            return """
                    {"code":500,"msg":"query failed"}
                    """;
        }
        if (current == null) {
            return """
                    {"code":200,"data":{"confData":{}}}
                    """;
        }
        return """
                {"code":200,"data":{"confData":{"payments":{"feature.enabled":{"env":"test","appname":"payments","key":"feature.enabled","value":"%s","valueMd5":"test"}}}}}
                """.formatted(current);
    }

    static final class FeatureFlags {
        private final List<String> events = new CopyOnWriteArrayList<>();

        @DynamicConfig(value = "${feature.key}", defaultValue = "true")
        private volatile boolean enabled;

        @DynamicConfig(value = "feature.enabled", defaultValue = "")
        void flagChanged(String updated) {
            events.add(updated);
        }
    }
}
