/*
 * #%L
 * LoadUp Components SpringDoc
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
package io.github.loadup.components.springdoc.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class SpringDocHttpIT {

    private static ConfigurableApplicationContext context;
    private static HttpClient client;
    private static String baseUrl;

    @BeforeAll
    static void startServer() {
        context = SpringApplication.run(
                TestApplication.class,
                "--server.port=0",
                "--server.address=127.0.0.1",
                "--spring.main.banner-mode=off",
                "--loadup.springdoc.title=Integration API",
                "--loadup.springdoc.description=Embedded server contract",
                "--loadup.springdoc.version=1.2.3",
                "--loadup.springdoc.jwt-scheme-name=IntegrationToken",
                "--springdoc.packages-to-scan=io.github.loadup.components.springdoc.autoconfigure");
        baseUrl = "http://127.0.0.1:" + context.getEnvironment().getRequiredProperty("local.server.port");
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @AfterAll
    static void stopServer() {
        if (client != null) {
            client.close();
        }
        if (context != null) {
            context.close();
        }
    }

    @Test
    void generatedDocumentCombinesMetadataSecurityAndControllerOperations() throws Exception {
        HttpResponse<String> response = get("/v3/api-docs");
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.headers().firstValue("Content-Type"))
                .hasValueSatisfying(value -> assertThat(value).startsWith("application/json"));
        DocumentContext document = JsonPath.parse(response.body());
        assertThat(document.<String>read("$.openapi")).startsWith("3.");
        assertThat(document.<String>read("$.info.title")).isEqualTo("Integration API");
        assertThat(document.<String>read("$.info.description")).isEqualTo("Embedded server contract");
        assertThat(document.<String>read("$.info.version")).isEqualTo("1.2.3");
        assertThat(document.<String>read("$.components.securitySchemes.IntegrationToken.type"))
                .isEqualTo("http");
        assertThat(document.<String>read("$.components.securitySchemes.IntegrationToken.scheme"))
                .isEqualTo("bearer");
        assertThat(document.<String>read("$.components.securitySchemes.IntegrationToken.bearerFormat"))
                .isEqualTo("JWT");
        assertThat(document.<Map<String, Object>>read("$.security[0]")).containsOnlyKeys("IntegrationToken");
        assertThat(document.<String>read("$.paths['/api/test/echo'].post.operationId"))
                .isEqualTo("echoMessage");
        assertThat(document.<String>read("$.paths['/api/test/echo'].post.summary"))
                .isEqualTo("Echo a test message");
        assertThat(document.<Boolean>read("$.paths['/api/test/echo'].post.requestBody.required"))
                .isTrue();
        assertThat(document.<Map<String, Object>>read("$.paths['/api/test/echo'].post.responses"))
                .containsKey("200");
        assertThat(document.<String>read("$.components.schemas.EchoInput.properties.message.description"))
                .isEqualTo("Message to echo");
    }

    @Test
    void documentedOperationActuallyAcceptsJsonOverHttp() throws Exception {
        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/api/test/echo"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"message\":\"integration message\"}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(JsonPath.parse(response.body()).<String>read("$.message")).isEqualTo("integration message");
    }

    @Test
    void unknownDocumentationGroupReturnsHttpNotFound() throws Exception {
        assertThat(get("/v3/api-docs/nonexistent-group").statusCode()).isEqualTo(404);
    }

    private static HttpResponse<String> get(String path) throws Exception {
        return client.send(
                HttpRequest.newBuilder(URI.create(baseUrl + path))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(EchoController.class)
    static class TestApplication {}

    @RestController
    static class EchoController {
        @Operation(operationId = "echoMessage", summary = "Echo a test message")
        @PostMapping("/api/test/echo")
        public Map<String, String> echo(@RequestBody EchoInput input) {
            return Map.of("message", input.message());
        }
    }

    record EchoInput(
            @Schema(description = "Message to echo", example = "hello")
            String message) {}
}
