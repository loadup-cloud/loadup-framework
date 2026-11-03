/*
 * #%L
 * LoadUp Components HTTP
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
package io.github.loadup.components.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class HttpClientDefaultsTest {
    public record Settings(String tenantId, String serviceId, String audience) {}

    @Test
    void targetAudienceStaysSeparateFromCallerIdentity() {
        var environment = new MockEnvironment()
                .withProperty("spring.application.name", "payment")
                .withProperty("loadup.http.client.tenant-id", "tenant");
        var acquire = HttpClientDefaults.bind(environment, "loadup.modules.payment.acquire", "acquire", Settings.class);
        var realtime =
                HttpClientDefaults.bind(environment, "loadup.modules.payment.realtime", "realtime", Settings.class);
        assertThat(acquire.tenantId()).isEqualTo("tenant");
        assertThat(acquire.serviceId()).isEqualTo("payment");
        assertThat(acquire.audience()).isEqualTo("acquire");
        assertThat(realtime.audience()).isEqualTo("realtime");
    }

    @Test
    void explicitApplicationSettingsOverrideSharedDefaults() {
        var environment = new MockEnvironment()
                .withProperty("loadup.http.client.tenant-id", "shared")
                .withProperty("loadup.http.client.service-id", "default-caller")
                .withProperty("loadup.http.client.audience", "default-target")
                .withProperty("loadup.modules.payment.acquire.tenant-id", "specific")
                .withProperty("loadup.modules.payment.acquire.audience", "acquire-api");
        var settings =
                HttpClientDefaults.bind(environment, "loadup.modules.payment.acquire", "acquire", Settings.class);
        assertThat(settings.tenantId()).isEqualTo("specific");
        assertThat(settings.serviceId()).isEqualTo("default-caller");
        assertThat(settings.audience()).isEqualTo("acquire-api");
    }
}
