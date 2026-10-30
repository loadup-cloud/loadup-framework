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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.apollo.ApolloConfigCenterProperties;
import io.github.loadup.components.configcenter.apollo.ApolloConfigCenterProvider;
import io.github.loadup.components.configcenter.apollo.autoconfigure.ApolloConfigCenterAutoConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Unit contracts at the Apollo SDK boundary; no Apollo server is contacted.
 */
@ResourceLock(Resources.SYSTEM_PROPERTIES)
class ApolloConfigCenterProviderTest {
    private final Map<String, String> previous = new HashMap<>();

    @BeforeEach
    void saveSystemProperties() {
        for (String key : List.of("app.id", "apollo.meta", "env", "apollo.cluster")) {
            previous.put(key, System.getProperty(key));
        }
    }

    @AfterEach
    void restoreSystemProperties() {
        previous.forEach((key, value) -> {
            if (value == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, value);
            }
        });
    }

    private ApolloConfigCenterProperties settings() {
        var properties = new ApolloConfigCenterProperties();
        properties.setAppId("synthetic-unit-app");
        return properties;
    }

    @Test
    void readsLiveValuesListsOnlyMatchingKeysAndRejectsClientWrites() {
        Config sdk = mock(Config.class);
        when(sdk.getProperty("payment.timeout", null)).thenReturn("1000");
        when(sdk.getPropertyNames()).thenReturn(Set.of("payment.timeout", "other", "payment.mode"));
        try (var factory = mockStatic(ConfigService.class)) {
            factory.when(() -> ConfigService.getConfig("application")).thenReturn(sdk);
            var provider = new ApolloConfigCenterProvider(settings());
            assertThat(provider.getBinderType()).isEqualTo("apollo");
            assertThat(provider.getConfig("payment.timeout")).isEqualTo("1000");
            assertThat(provider.getConfig("missing")).isNull();
            assertThat(provider.listKeys("payment.")).containsExactly("payment.mode", "payment.timeout");
            assertThat(provider.listKeys("missing.")).isEmpty();
            when(sdk.getProperty("payment.timeout", null)).thenReturn("2000");
            assertThat(provider.getConfig("payment.timeout")).isEqualTo("2000");
            assertThat(provider.setConfig("payment.timeout", "3000")).isFalse();
            assertThat(provider.removeConfig("payment.timeout")).isFalse();
            assertThat(provider.getConfig("payment.timeout")).isEqualTo("2000");
            assertThat(System.getProperty("app.id")).isEqualTo("synthetic-unit-app");
        }
    }

    @Test
    void changesFanOutOnlyToRegisteredKeysAndRemovalStopsCallbacks() {
        Config sdk = mock(Config.class);
        try (var factory = mockStatic(ConfigService.class)) {
            factory.when(() -> ConfigService.getConfig("application")).thenReturn(sdk);
            var provider = new ApolloConfigCenterProvider(settings());
            var capture = ArgumentCaptor.forClass(ConfigChangeListener.class);
            verify(sdk).addChangeListener(capture.capture());
            List<String> first = new ArrayList<>(), second = new ArrayList<>();
            provider.addListener("key", first::add);
            provider.addListener("key", second::add);
            capture.getValue()
                    .onChange(new ConfigChangeEvent(
                            "unit-app",
                            "application",
                            Map.of(
                                    "key",
                                    new ConfigChange(
                                            "unit-app",
                                            "application",
                                            "key",
                                            "old",
                                            "new",
                                            PropertyChangeType.MODIFIED),
                                    "unwatched",
                                    new ConfigChange(
                                            "unit-app",
                                            "application",
                                            "unwatched",
                                            null,
                                            "value",
                                            PropertyChangeType.ADDED))));
            assertThat(first).containsExactly("new");
            assertThat(second).containsExactly("new");
            capture.getValue()
                    .onChange(new ConfigChangeEvent(
                            "unit-app",
                            "application",
                            Map.of(
                                    "key",
                                    new ConfigChange(
                                            "unit-app",
                                            "application",
                                            "key",
                                            "new",
                                            null,
                                            PropertyChangeType.DELETED))));
            assertThat(first).containsExactly("new", null);
            provider.removeListener("key");
            capture.getValue()
                    .onChange(new ConfigChangeEvent(
                            "unit-app",
                            "application",
                            Map.of(
                                    "key",
                                    new ConfigChange(
                                            "unit-app",
                                            "application",
                                            "key",
                                            null,
                                            "after",
                                            PropertyChangeType.ADDED))));
            assertThat(second).containsExactly("new", null);
            provider.addListener("key", first::add);
            capture.getValue()
                    .onChange(new ConfigChangeEvent(
                            "unit-app",
                            "application",
                            Map.of(
                                    "key",
                                    new ConfigChange(
                                            "unit-app",
                                            "application",
                                            "key",
                                            "after",
                                            "replaced",
                                            PropertyChangeType.MODIFIED))));
            assertThat(first).containsExactly("new", null, "replaced");
            assertThat(second).containsExactly("new", null);
        }
    }

    @Test
    void nullOptionalSettingsPreserveExistingSdkEnvironment() {
        var properties = settings();
        properties.setMeta(null);
        properties.setEnv(null);
        properties.setCluster(null);
        System.setProperty("apollo.meta", "http://existing.invalid");
        System.setProperty("env", "EXISTING");
        System.setProperty("apollo.cluster", "existing-cluster");
        Config sdk = mock(Config.class);
        try (var factory = mockStatic(ConfigService.class)) {
            factory.when(() -> ConfigService.getConfig("application")).thenReturn(sdk);
            var provider = new ApolloConfigCenterProvider(properties);
            assertThat(provider.getBinderType()).isEqualTo("apollo");
            assertThat(System.getProperty("apollo.meta")).isEqualTo("http://existing.invalid");
            assertThat(System.getProperty("env")).isEqualTo("EXISTING");
            assertThat(System.getProperty("apollo.cluster")).isEqualTo("existing-cluster");
        }
    }

    @Test
    void bindsSdkSettingsAndBacksOffForAnotherBinderOrSuppliedProvider() {
        Config sdk = mock(Config.class);
        var runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ApolloConfigCenterAutoConfiguration.class))
                .withPropertyValues(
                        "loadup.configcenter.binder-type=apollo",
                        "loadup.configcenter.binder.apollo.app-id=bound-app",
                        "loadup.configcenter.binder.apollo.meta=http://meta.invalid",
                        "loadup.configcenter.binder.apollo.env=TEST",
                        "loadup.configcenter.binder.apollo.cluster=bound-cluster",
                        "loadup.configcenter.binder.apollo.namespace=payments");
        try (var factory = mockStatic(ConfigService.class)) {
            factory.when(() -> ConfigService.getConfig("payments")).thenReturn(sdk);
            runner.run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(ConfigCenterProvider.class);
                assertThat(context.getBean(ConfigCenterProvider.class)).isInstanceOf(ApolloConfigCenterProvider.class);
                assertThat(context.getBean(ApolloConfigCenterProperties.class).getNamespace())
                        .isEqualTo("payments");
                assertThat(System.getProperty("app.id")).isEqualTo("bound-app");
                assertThat(System.getProperty("apollo.meta")).isEqualTo("http://meta.invalid");
                assertThat(System.getProperty("env")).isEqualTo("TEST");
                assertThat(System.getProperty("apollo.cluster")).isEqualTo("bound-cluster");
            });
            ConfigCenterProvider custom = mock(ConfigCenterProvider.class);
            runner.withBean(ConfigCenterProvider.class, () -> custom)
                    .run(context -> assertThat(context.getBean(ConfigCenterProvider.class))
                            .isSameAs(custom));
            runner.withPropertyValues("loadup.configcenter.binder-type=local")
                    .run(context -> assertThat(context).doesNotHaveBean(ConfigCenterProvider.class));
            factory.verify(() -> ConfigService.getConfig("payments"), times(1));
        }
    }

    @Test
    void preservesUnexpectedSdkFailureForDiagnostics() {
        Config sdk = mock(Config.class);
        IllegalStateException failure = new IllegalStateException("synthetic SDK failure");
        when(sdk.getProperty(eq("key"), any())).thenThrow(failure);
        try (var factory = mockStatic(ConfigService.class)) {
            factory.when(() -> ConfigService.getConfig("application")).thenReturn(sdk);
            var provider = new ApolloConfigCenterProvider(settings());
            assertThatThrownBy(() -> provider.getConfig("key")).isSameAs(failure);
        }
    }
}
