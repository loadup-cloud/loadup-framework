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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.nacos.NacosConfigCenterProperties;
import io.github.loadup.components.configcenter.nacos.NacosConfigCenterProvider;
import io.github.loadup.components.configcenter.nacos.autoconfigure.NacosConfigCenterAutoConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Unit contracts with a mocked Nacos SDK, not a remote-server integration test.
 */
class NacosConfigCenterProviderContractTest {
    private static final String DATA_ID = "loadup-config", GROUP = "DEFAULT_GROUP";

    @Test
    void readsInitialSnapshotAndListsMatchingKeysInOrder() throws Exception {
        ConfigService sdk = mock(ConfigService.class);
        when(sdk.getConfig(DATA_ID, GROUP, 3000)).thenReturn("payment.timeout=1000\npayment.mode=sync\nother=value");
        try (var factory = mockStatic(NacosFactory.class)) {
            factory.when(() -> NacosFactory.createConfigService(any(Properties.class)))
                    .thenReturn(sdk);
            var provider = new NacosConfigCenterProvider(new NacosConfigCenterProperties());
            assertThat(provider.getBinderType()).isEqualTo("nacos");
            assertThat(provider.getConfig("payment.timeout")).isEqualTo("1000");
            assertThat(provider.getConfig("missing")).isNull();
            assertThat(provider.listKeys("payment.")).containsExactly("payment.mode", "payment.timeout");
            assertThat(provider.listKeys("unmatched")).isEmpty();
        }
    }

    @Test
    void successfulWritesReadLatestServerStateAndPublishWholeDocument() throws Exception {
        ConfigService sdk = mock(ConfigService.class);
        when(sdk.getConfig(DATA_ID, GROUP, 3000))
                .thenReturn("a=old", "a=old\nexternal=retained", "a=new\nexternal=retained");
        when(sdk.publishConfig(eq(DATA_ID), eq(GROUP), anyString())).thenReturn(true);
        try (var factory = mockStatic(NacosFactory.class)) {
            factory.when(() -> NacosFactory.createConfigService(any(Properties.class)))
                    .thenReturn(sdk);
            var provider = new NacosConfigCenterProvider(new NacosConfigCenterProperties());
            assertThat(provider.setConfig("a", "new")).isTrue();
            assertThat(provider.getConfig("a")).isEqualTo("new");
            assertThat(provider.getConfig("external")).isEqualTo("retained");
            assertThat(provider.removeConfig("a")).isTrue();
            assertThat(provider.getConfig("a")).isNull();
            assertThat(provider.getConfig("external")).isEqualTo("retained");
            var contents = ArgumentCaptor.forClass(String.class);
            verify(sdk, times(2)).publishConfig(eq(DATA_ID), eq(GROUP), contents.capture());
            Properties first = new Properties(), second = new Properties();
            first.load(new java.io.StringReader(contents.getAllValues().get(0)));
            second.load(new java.io.StringReader(contents.getAllValues().get(1)));
            assertThat(first).containsEntry("a", "new").containsEntry("external", "retained");
            assertThat(second).containsEntry("external", "retained").doesNotContainKey("a");
        }
    }

    @Test
    void rejectedAndFailedWritesDoNotMutatePublishedSnapshot() throws Exception {
        ConfigService sdk = mock(ConfigService.class);
        when(sdk.getConfig(DATA_ID, GROUP, 3000)).thenReturn("a=old");
        when(sdk.publishConfig(eq(DATA_ID), eq(GROUP), anyString()))
                .thenReturn(false)
                .thenThrow(new NacosException(500, "synthetic publish failure"));
        try (var factory = mockStatic(NacosFactory.class)) {
            factory.when(() -> NacosFactory.createConfigService(any(Properties.class)))
                    .thenReturn(sdk);
            var provider = new NacosConfigCenterProvider(new NacosConfigCenterProperties());
            assertThat(provider.setConfig("a", "new")).isFalse();
            assertThat(provider.removeConfig("a")).isFalse();
            assertThat(provider.getConfig("a")).isEqualTo("old");
            doReturn(false).when(sdk).publishConfig(eq(DATA_ID), eq(GROUP), anyString());
            assertThat(provider.removeConfig("a")).isFalse();
            doThrow(new NacosException(500, "write failed"))
                    .when(sdk)
                    .publishConfig(eq(DATA_ID), eq(GROUP), anyString());
            assertThat(provider.setConfig("a", "new")).isFalse();
            when(sdk.getConfig(DATA_ID, GROUP, 3000)).thenThrow(new NacosException(500, "read failed"));
            assertThat(provider.setConfig("a", "new")).isFalse();
            assertThat(provider.removeConfig("a")).isFalse();
            assertThat(provider.getConfig("a")).isEqualTo("old");
        }
    }

    @Test
    void removingMissingKeyIsSuccessfulWithoutPublishing() throws Exception {
        ConfigService sdk = mock(ConfigService.class);
        when(sdk.getConfig(DATA_ID, GROUP, 3000)).thenReturn("a=old");
        try (var factory = mockStatic(NacosFactory.class)) {
            factory.when(() -> NacosFactory.createConfigService(any(Properties.class)))
                    .thenReturn(sdk);
            var provider = new NacosConfigCenterProvider(new NacosConfigCenterProperties());
            assertThat(provider.removeConfig("absent")).isTrue();
            assertThat(provider.getConfig("a")).isEqualTo("old");
            verify(sdk, never()).publishConfig(anyString(), anyString(), anyString());
        }
    }

    @Test
    void singleSdkListenerRefreshesChangedAddedAndDeletedKeysAndHonorsUnsubscribe() throws Exception {
        ConfigService sdk = mock(ConfigService.class);
        when(sdk.getConfig(DATA_ID, GROUP, 3000)).thenReturn("changed=old\nremoved=old\nstable=same");
        try (var factory = mockStatic(NacosFactory.class)) {
            factory.when(() -> NacosFactory.createConfigService(any(Properties.class)))
                    .thenReturn(sdk);
            var provider = new NacosConfigCenterProvider(new NacosConfigCenterProperties());
            List<String> changed = new ArrayList<>(),
                    additional = new ArrayList<>(),
                    removed = new ArrayList<>(),
                    stable = new ArrayList<>();
            provider.addListener("changed", changed::add);
            provider.addListener("changed", additional::add);
            provider.addListener("removed", removed::add);
            provider.addListener("stable", stable::add);
            var capture = ArgumentCaptor.forClass(Listener.class);
            verify(sdk).addListener(eq(DATA_ID), eq(GROUP), capture.capture());
            Listener listener = capture.getValue();
            listener.getExecutor().execute(() -> listener.receiveConfigInfo("changed=new\nstable=same\nadded=value"));
            assertThat(changed).containsExactly("new");
            assertThat(additional).containsExactly("new");
            assertThat(removed).containsExactly((String) null);
            assertThat(stable).isEmpty();
            assertThat(provider.getConfig("added")).isEqualTo("value");
            assertThat(provider.getConfig("removed")).isNull();
            listener.receiveConfigInfo("changed=new\nstable=same\nadded=value");
            assertThat(changed).containsExactly("new");
            provider.removeListener("changed");
            listener.receiveConfigInfo("changed=after\nstable=same\nadded=value");
            assertThat(changed).containsExactly("new");
            assertThat(additional).containsExactly("new");
            assertThat(provider.getConfig("changed")).isEqualTo("after");
        }
    }

    @Test
    void checkedSdkCreationAndListenerFailuresRetainSystemErrorCode() throws Exception {
        NacosException failure = new NacosException(500, "synthetic unavailable");
        try (var factory = mockStatic(NacosFactory.class)) {
            factory.when(() -> NacosFactory.createConfigService(any(Properties.class)))
                    .thenThrow(failure);
            assertThatThrownBy(() -> new NacosConfigCenterProvider(new NacosConfigCenterProperties()))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            error -> assertThat(error.getResultCode()).isEqualTo(CommonResultCodeEnum.SYS_ERROR))
                    .hasCause(failure);
        }
        ConfigService sdk = mock(ConfigService.class);
        doThrow(failure).when(sdk).addListener(eq(DATA_ID), eq(GROUP), any(Listener.class));
        try (var factory = mockStatic(NacosFactory.class)) {
            factory.when(() -> NacosFactory.createConfigService(any(Properties.class)))
                    .thenReturn(sdk);
            var provider = new NacosConfigCenterProvider(new NacosConfigCenterProperties());
            assertThatThrownBy(() -> provider.addListener("key", value -> {}))
                    .isInstanceOfSatisfying(
                            CommonException.class,
                            error -> assertThat(error.getResultCode()).isEqualTo(CommonResultCodeEnum.SYS_ERROR))
                    .hasCause(failure);
        }
    }

    @Test
    void initialSnapshotFailureFailsFastWithOriginalCause() throws Exception {
        ConfigService sdk = mock(ConfigService.class);
        NacosException failure = new NacosException(500, "synthetic initial load failure");
        when(sdk.getConfig(DATA_ID, GROUP, 3000)).thenThrow(failure);
        try (var factory = mockStatic(NacosFactory.class)) {
            factory.when(() -> NacosFactory.createConfigService(any(Properties.class)))
                    .thenReturn(sdk);
            assertThatThrownBy(() -> new NacosConfigCenterProvider(new NacosConfigCenterProperties()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasCause(failure);
        }
    }

    @Test
    void bindsAllSdkOptionsAndHonorsConsumerProviderAndBinderSelection() throws Exception {
        ConfigService sdk = mock(ConfigService.class);
        when(sdk.getConfig("payments.yaml", "PAYMENTS", 1200)).thenReturn("payment:\n  timeout: 42");
        AtomicReference<Properties> captured = new AtomicReference<>();
        var runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(NacosConfigCenterAutoConfiguration.class))
                .withPropertyValues(
                        "loadup.configcenter.binder-type=nacos",
                        "loadup.configcenter.binder.nacos.server-addr=127.0.0.1:18848",
                        "loadup.configcenter.binder.nacos.namespace=unit-space",
                        "loadup.configcenter.binder.nacos.username=synthetic-user",
                        "loadup.configcenter.binder.nacos.password=synthetic-password",
                        "loadup.configcenter.binder.nacos.data-id=payments.yaml",
                        "loadup.configcenter.binder.nacos.group=PAYMENTS",
                        "loadup.configcenter.binder.nacos.timeout=1200",
                        "loadup.configcenter.binder.nacos.file-extension=yaml");
        try (var factory = mockStatic(NacosFactory.class)) {
            factory.when(() -> NacosFactory.createConfigService(any(Properties.class)))
                    .thenAnswer(invocation -> {
                        captured.set(invocation.getArgument(0));
                        return sdk;
                    });
            runner.run(context -> {
                assertThat(context).hasNotFailed().hasSingleBean(ConfigCenterProvider.class);
                assertThat(context.getBean(ConfigCenterProvider.class).getConfig("payment.timeout"))
                        .isEqualTo("42");
                assertThat(captured.get())
                        .containsEntry("serverAddr", "127.0.0.1:18848")
                        .containsEntry("namespace", "unit-space")
                        .containsEntry("username", "synthetic-user")
                        .containsEntry("password", "synthetic-password");
            });
            ConfigCenterProvider custom = mock(ConfigCenterProvider.class);
            runner.withBean(ConfigCenterProvider.class, () -> custom)
                    .run(context -> assertThat(context.getBean(ConfigCenterProvider.class))
                            .isSameAs(custom));
            runner.withPropertyValues("loadup.configcenter.binder-type=local")
                    .run(context -> assertThat(context).doesNotHaveBean(ConfigCenterProvider.class));
        }
    }

    @Test
    void nullOrBlankNamespaceDoesNotOverrideSdkDefaultNamespace() throws Exception {
        ConfigService sdk = mock(ConfigService.class);
        AtomicReference<Properties> captured = new AtomicReference<>();
        try (var factory = mockStatic(NacosFactory.class)) {
            factory.when(() -> NacosFactory.createConfigService(any(Properties.class)))
                    .thenAnswer(invocation -> {
                        captured.set(invocation.getArgument(0));
                        return sdk;
                    });
            for (String namespace : new String[] {null, " "}) {
                var properties = new NacosConfigCenterProperties();
                properties.setNamespace(namespace);
                var provider = new NacosConfigCenterProvider(properties);
                assertThat(provider.listKeys("")).isEmpty();
                assertThat(captured.get()).doesNotContainKeys("namespace", "username", "password");
            }
        }
    }
}
