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
package io.github.loadup.components.gotone.test.store;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;
import io.github.loadup.components.gotone.NotificationService;
import io.github.loadup.components.gotone.config.ChannelConfigProvider;
import io.github.loadup.components.gotone.config.ServiceConfigProvider;
import io.github.loadup.components.gotone.model.NotificationRequest;
import io.github.loadup.components.gotone.model.NotificationResponse;
import io.github.loadup.components.gotone.store.dataobject.NotificationServiceDO;
import io.github.loadup.components.gotone.store.dataobject.ServiceChannelDO;
import io.github.loadup.components.gotone.store.mapper.NotificationServiceDOMapper;
import io.github.loadup.components.gotone.store.mapper.ServiceChannelDOMapper;
import io.github.loadup.components.gotone.test.TestGotoneApplication;
import io.github.loadup.components.testcontainers.annotation.ContainerType;
import io.github.loadup.components.testcontainers.annotation.EnableTestContainers;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Full-stack integration test: JDBC store + engine + webhook binder + Flyway schema.
 */
@SpringBootTest(classes = TestGotoneApplication.class)
@EnableTestContainers(ContainerType.MYSQL)
@ActiveProfiles("test")
class JdbcStoreIT {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ChannelConfigProvider channelConfigProvider;

    @Autowired
    private ServiceConfigProvider serviceConfigProvider;

    @Autowired
    private NotificationServiceDOMapper serviceMapper;

    @Autowired
    private ServiceChannelDOMapper channelMapper;

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void sendsViaStoredConfigAndPersistsRecords() throws IOException {
        String serviceCode =
                "SYSTEM_ALERT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/robot", exchange -> {
            byte[] response = "{\"errcode\":0}".getBytes(UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();

        insertService(serviceCode);
        insertChannel(serviceCode, "http://localhost:" + server.getAddress().getPort() + "/robot");

        assertThat(serviceConfigProvider.findByServiceCode(serviceCode)).isPresent();
        assertThat(channelConfigProvider.findEnabledByServiceCode(serviceCode)).hasSize(1);

        NotificationResponse response = notificationService.send(
                NotificationRequest.of(serviceCode, List.of("ops@example.com"), Map.of("errorMessage", "db is down")));

        assertThat(response.success()).isTrue();
        assertThat(response.channelResults().get(0).provider()).isEqualTo("dingtalk");
    }

    private void insertService(String serviceCode) {
        NotificationServiceDO entity = new NotificationServiceDO();
        entity.setId(UUID.randomUUID().toString());
        entity.setServiceCode(serviceCode);
        entity.setServiceName("System Alert");
        entity.setEnabled(true);
        entity.setPriority(10);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        serviceMapper.insert(entity);
    }

    private void insertChannel(String serviceCode, String webhookUrl) {
        ServiceChannelDO entity = new ServiceChannelDO();
        entity.setId(UUID.randomUUID().toString());
        entity.setServiceCode(serviceCode);
        entity.setChannel("WEBHOOK");
        entity.setProvider("dingtalk");
        entity.setTemplateContent("Alert: ${errorMessage}");
        entity.setChannelConfig("{\"webhookUrl\":\"" + webhookUrl + "\"}");
        entity.setFallbackProviders("[]");
        entity.setEnabled(true);
        entity.setPriority(10);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        channelMapper.insert(entity);
    }
}
