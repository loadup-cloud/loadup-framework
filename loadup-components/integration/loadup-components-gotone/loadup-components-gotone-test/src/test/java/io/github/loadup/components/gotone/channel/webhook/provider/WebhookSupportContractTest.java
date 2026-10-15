/*
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
package io.github.loadup.components.gotone.channel.webhook.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.channel.webhook.config.WebhookChannelAutoConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class WebhookSupportContractTest {
    @Test
    void configurationDefaultsAndDiagnosticMaskingHandleMissingValues() {
        assertThat(WebhookSupport.configValue(null, "key", "default")).isEqualTo("default");
        assertThat(WebhookSupport.configValue(Map.of(), "key", "default")).isEqualTo("default");
        Map<String, Object> config = new HashMap<>();
        config.put("key", null);
        assertThat(WebhookSupport.configValue(config, "key", "default")).isEqualTo("default");
        config.put("key", 42);
        assertThat(WebhookSupport.configValue(config, "key", "default")).isEqualTo("42");
        assertThat(WebhookSupport.maskUrl(null)).isEqualTo("***");
        assertThat(WebhookSupport.maskUrl("short")).isEqualTo("***");
        assertThat(WebhookSupport.maskUrl("x".repeat(50))).isEqualTo("x".repeat(40) + "...");
    }

    @Test
    void providerBeansCoexistAndAllowPerVendorDisableAndOverride() {
        var contexts = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(WebhookChannelAutoConfiguration.class));
        contexts.run(context -> assertThat(context.getBeansOfType(NotificationChannelProvider.class))
                .hasSize(3));
        var custom = mock(NotificationChannelProvider.class);
        contexts.withBean("wechatWebhookProvider", NotificationChannelProvider.class, () -> custom)
                .withPropertyValues(
                        "loadup.gotone.binder.webhook.dingtalk.enabled=false",
                        "loadup.gotone.binder.webhook.feishu.enabled=false")
                .run(context -> {
                    assertThat(context.getBeansOfType(NotificationChannelProvider.class))
                            .containsOnlyKeys("wechatWebhookProvider");
                    assertThat(context.getBean("wechatWebhookProvider")).isSameAs(custom);
                });
    }
}
