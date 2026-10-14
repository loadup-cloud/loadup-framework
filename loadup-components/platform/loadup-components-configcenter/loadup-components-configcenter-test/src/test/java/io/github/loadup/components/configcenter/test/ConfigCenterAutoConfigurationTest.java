/*
 * #%L
 * LoadUp ConfigCenter Test
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
package io.github.loadup.components.configcenter.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.loadup.components.configcenter.ConfigCenterProperties;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.ConfigCenterTemplate;
import io.github.loadup.components.configcenter.autoconfigure.ConfigCenterAutoConfiguration;
import io.github.loadup.components.configcenter.local.LocalConfigCenterProperties;
import io.github.loadup.components.configcenter.local.LocalConfigCenterProvider;
import io.github.loadup.components.configcenter.local.autoconfigure.LocalConfigCenterAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class ConfigCenterAutoConfigurationTest {
    private final ApplicationContextRunner api = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigCenterAutoConfiguration.class));
    private final ApplicationContextRunner local = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    LocalConfigCenterAutoConfiguration.class, ConfigCenterAutoConfiguration.class));

    @Test
    void defaultLocalBindingProducesAWorkingTemplate() {
        local.run(context -> {
            assertThat(context)
                    .hasNotFailed()
                    .hasSingleBean(ConfigCenterProvider.class)
                    .hasSingleBean(ConfigCenterTemplate.class)
                    .hasSingleBean(LocalConfigCenterProperties.class);
            assertThat(context.getBean(ConfigCenterProvider.class)).isInstanceOf(LocalConfigCenterProvider.class);
            assertThat(context.getBean(ConfigCenterProperties.class).getBinderType())
                    .isEqualTo("local");
            ConfigCenterTemplate template = context.getBean(ConfigCenterTemplate.class);
            template.setConfig("runtime.key", "value");
            assertThat(template.getConfig("runtime.key")).isEqualTo("value");
        });
    }

    @Test
    void noProviderOrAmbiguousProvidersDoNotPublishATemplate() {
        api.run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(ConfigCenterTemplate.class));
        api.withBean("first", ConfigCenterProvider.class, LocalConfigCenterProvider::new)
                .withBean("second", ConfigCenterProvider.class, LocalConfigCenterProvider::new)
                .run(context -> assertThat(context).hasNotFailed().doesNotHaveBean(ConfigCenterTemplate.class));
    }

    @Test
    void singleProviderBindsApiPropertiesAndPrimaryProviderResolvesAmbiguity() {
        ConfigCenterProvider provider = mock(ConfigCenterProvider.class);
        when(provider.getConfig("key")).thenReturn("primary value");
        api.withBean(ConfigCenterProvider.class, () -> provider)
                .withPropertyValues("loadup.configcenter.binder-type=custom")
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(ConfigCenterTemplate.class);
                    assertThat(context.getBean(ConfigCenterProperties.class).getBinderType())
                            .isEqualTo("custom");
                    assertThat(context.getBean(ConfigCenterTemplate.class).getConfig("key"))
                            .isEqualTo("primary value");
                });
        api.withBean("primary", ConfigCenterProvider.class, () -> provider, definition -> definition.setPrimary(true))
                .withBean("secondary", ConfigCenterProvider.class, LocalConfigCenterProvider::new)
                .run(context -> assertThat(
                                context.getBean(ConfigCenterTemplate.class).getConfig("key"))
                        .isEqualTo("primary value"));
    }

    @Test
    void customTemplateIsPreserved() {
        ConfigCenterTemplate custom = mock(ConfigCenterTemplate.class);
        local.withBean(ConfigCenterTemplate.class, () -> custom).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(ConfigCenterTemplate.class);
            assertThat(context.getBean(ConfigCenterTemplate.class)).isSameAs(custom);
        });
    }

    @Test
    void localBinderBacksOffForConsumerProviderAndAlternativeBinderType() {
        ConfigCenterProvider custom = mock(ConfigCenterProvider.class);
        local.withBean(ConfigCenterProvider.class, () -> custom).run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(ConfigCenterProvider.class);
            assertThat(context.getBean(ConfigCenterProvider.class)).isSameAs(custom);
            assertThat(context).doesNotHaveBean("localConfigCenterProvider");
        });
        local.withPropertyValues("loadup.configcenter.binder-type=nacos")
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .doesNotHaveBean(ConfigCenterProvider.class)
                        .doesNotHaveBean(ConfigCenterTemplate.class)
                        .doesNotHaveBean(LocalConfigCenterProperties.class));
    }
}
