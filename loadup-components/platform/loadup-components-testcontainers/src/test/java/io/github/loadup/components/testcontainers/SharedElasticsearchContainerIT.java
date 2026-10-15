/*
 * #%L
 * Loadup Components TestContainers
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
package io.github.loadup.components.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import io.github.loadup.components.testcontainers.search.AbstractElasticsearchContainerTest;
import io.github.loadup.components.testcontainers.search.SharedElasticsearchContainer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApplication.class)
@EnableTestContainers(ContainerType.ELASTICSEARCH)
class SharedElasticsearchContainerIT extends AbstractElasticsearchContainerTest {
    @Test
    void sharedContainerSupportsRealIndexWriteAndRead() throws Exception {
        assertThat(SharedElasticsearchContainer.getInstance().isRunning()).isTrue();
        assertThat(getHost()).isNotBlank();
        assertThat(getPort()).isPositive();
        assertThat(SharedElasticsearchContainer.isStarted()).isTrue();
        assertThat(SharedElasticsearchContainer.getProperties())
                .containsEntry("spring.elasticsearch.uris", getHttpHostAddress());
        try (var client =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build()) {
            String base = "http://" + getHttpHostAddress();
            var write = client.send(
                    HttpRequest.newBuilder(URI.create(base + "/loadup-coverage/_doc/1?refresh=true"))
                            .timeout(Duration.ofSeconds(20))
                            .header("Content-Type", "application/json")
                            .PUT(HttpRequest.BodyPublishers.ofString("{\"status\":\"ready\"}"))
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(write.statusCode()).isEqualTo(201);
            var read = client.send(
                    HttpRequest.newBuilder(URI.create(base + "/loadup-coverage/_doc/1"))
                            .timeout(Duration.ofSeconds(20))
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(read.statusCode()).isEqualTo(200);
            assertThat(read.body()).contains("ready");
            var delete = client.send(
                    HttpRequest.newBuilder(URI.create(base + "/loadup-coverage"))
                            .timeout(Duration.ofSeconds(20))
                            .DELETE()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());
            assertThat(delete.statusCode()).isEqualTo(200);
        }
    }
}
