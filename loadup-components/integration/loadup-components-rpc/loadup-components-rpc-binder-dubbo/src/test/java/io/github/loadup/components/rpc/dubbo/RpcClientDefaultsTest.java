/*
 * #%L
 * LoadUp Components RPC Binder Dubbo
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
package io.github.loadup.components.rpc.dubbo;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class RpcClientDefaultsTest {
    public record Settings(String tenantId, String serviceId, String audience, Path tokenFile) {}

    @Test
    void infersCallerAndTargetWithoutPerClientConfiguration() {
        var environment = new MockEnvironment().withProperty("spring.application.name", "payment");
        var settings = RpcClientDefaults.bind(
                environment, "loadup.modules.payment.limitcenter", "limitcenter", Settings.class);
        assertThat(settings)
                .isEqualTo(new Settings(
                        "__default__", "payment", "limitcenter", Path.of("/var/run/loadup/secrets/limitcenter.jwt")));
    }

    @Test
    void sharedSettingsApplyToEveryClientAndSpecificSettingsOverrideOneField() {
        var environment = new MockEnvironment()
                .withProperty("spring.application.name", "payment")
                .withProperty("loadup.rpc.client.tenant-id", "tenant_a")
                .withProperty("loadup.rpc.client.service-id", "custom-caller")
                .withProperty("loadup.rpc.client.audience", "internal")
                .withProperty("loadup.rpc.client.token-file", "/tmp/service.jwt")
                .withProperty("loadup.modules.payment.limitcenter.audience", "limit-api");
        var limit = RpcClientDefaults.bind(
                environment, "loadup.modules.payment.limitcenter", "limitcenter", Settings.class);
        var acquire = RpcClientDefaults.bind(environment, "loadup.modules.payment.acquire", "acquire", Settings.class);
        assertThat(limit)
                .isEqualTo(new Settings("tenant_a", "custom-caller", "limit-api", Path.of("/tmp/service.jwt")));
        assertThat(acquire)
                .isEqualTo(new Settings("tenant_a", "custom-caller", "internal", Path.of("/tmp/service.jwt")));
    }

    @Test
    void resolvesPlaceholdersInOverrides() {
        var environment = new MockEnvironment()
                .withProperty("spring.application.name", "payment")
                .withProperty("special-audience", "private-api")
                .withProperty("loadup.modules.payment.limitcenter.audience", "${special-audience}");
        var settings = RpcClientDefaults.bind(
                environment, "loadup.modules.payment.limitcenter", "limitcenter", Settings.class);
        assertThat(settings.audience()).isEqualTo("private-api");
    }
}
