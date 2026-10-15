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
package io.github.loadup.components.gotone.test.channel.push;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.channel.push.FcmPushConfig;
import io.github.loadup.components.gotone.channel.push.FcmPushProvider;
import io.github.loadup.components.gotone.channel.push.config.PushChannelAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class PushChannelAutoConfigurationTest {
    private final ApplicationContextRunner contexts =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(PushChannelAutoConfiguration.class));

    @Test
    void bindsCredentialsAndProjectIntoTypedConfiguration() {
        contexts.withPropertyValues(
                        "loadup.gotone.binder.push.fcm.server-key=synthetic-key",
                        "loadup.gotone.binder.push.fcm.project-id=project-one")
                .run(context -> {
                    assertThat(context).hasSingleBean(FcmPushConfig.class);
                    var configuration = context.getBean(FcmPushConfig.class);
                    assertThat(configuration.getServerKey()).isEqualTo("synthetic-key");
                    assertThat(configuration.getProjectId()).isEqualTo("project-one");
                    var provider = context.getBean("fcmPushProvider", NotificationChannelProvider.class);
                    assertThat(provider).isInstanceOf(FcmPushProvider.class);
                    assertThat(provider.getProviderName()).isEqualTo("fcm");
                    assertThat(provider.isAvailable()).isTrue();
                });
    }

    @Test
    void defaultsToUnavailableWhenCredentialsAreAbsent() {
        contexts.run(context -> {
            var config = context.getBean(FcmPushConfig.class);
            assertThat(config.getServerKey()).isNull();
            assertThat(config.getProjectId()).isNull();
            assertThat(context.getBean("fcmPushProvider", NotificationChannelProvider.class)
                            .isAvailable())
                    .isFalse();
        });
    }

    @Test
    void disabledBinderDoesNotRegisterConfigurationOrProvider() {
        contexts.withPropertyValues("loadup.gotone.binder.push.fcm.enabled=false")
                .run(context ->
                        assertThat(context).doesNotHaveBean(FcmPushConfig.class).doesNotHaveBean("fcmPushProvider"));
    }

    @Test
    void namedConsumerProviderReplacesDefaultProvider() {
        var custom = mock(NotificationChannelProvider.class);
        contexts.withBean("fcmPushProvider", NotificationChannelProvider.class, () -> custom)
                .run(context -> assertThat(context.getBean("fcmPushProvider")).isSameAs(custom));
    }
}
