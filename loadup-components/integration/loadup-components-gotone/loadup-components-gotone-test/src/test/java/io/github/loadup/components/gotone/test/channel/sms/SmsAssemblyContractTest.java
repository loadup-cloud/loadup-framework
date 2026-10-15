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
package io.github.loadup.components.gotone.test.channel.sms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.github.loadup.components.gotone.NotificationChannelProvider;
import io.github.loadup.components.gotone.channel.sms.AliyunSmsConfig;
import io.github.loadup.components.gotone.channel.sms.HuaweiSmsConfig;
import io.github.loadup.components.gotone.channel.sms.YunpianSmsConfig;
import io.github.loadup.components.gotone.channel.sms.config.SmsChannelAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class SmsAssemblyContractTest {
    private final ApplicationContextRunner contexts =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SmsChannelAutoConfiguration.class));

    @Test
    void allVendorOptionsBindAndProduceIndependentProviders() {
        contexts.withPropertyValues(
                        "loadup.gotone.binder.sms.aliyun.access-key-id=key",
                        "loadup.gotone.binder.sms.aliyun.access-key-secret=secret",
                        "loadup.gotone.binder.sms.aliyun.sign-name=sign",
                        "loadup.gotone.binder.sms.aliyun.region-id=region",
                        "loadup.gotone.binder.sms.huawei.app-key=key",
                        "loadup.gotone.binder.sms.huawei.app-secret=secret",
                        "loadup.gotone.binder.sms.huawei.sender=sender",
                        "loadup.gotone.binder.sms.huawei.signature=sign",
                        "loadup.gotone.binder.sms.huawei.endpoint=http://localhost/huawei",
                        "loadup.gotone.binder.sms.yunpian.api-key=key",
                        "loadup.gotone.binder.sms.yunpian.api-url=http://localhost/yunpian")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBeansOfType(NotificationChannelProvider.class))
                            .hasSize(3);
                    assertThat(context.getBean(AliyunSmsConfig.class).getRegionId())
                            .isEqualTo("region");
                    assertThat(context.getBean(AliyunSmsConfig.class).getSignName())
                            .isEqualTo("sign");
                    assertThat(context.getBean(HuaweiSmsConfig.class).getEndpoint())
                            .isEqualTo("http://localhost/huawei");
                    assertThat(context.getBean(HuaweiSmsConfig.class).getSender())
                            .isEqualTo("sender");
                    assertThat(context.getBean(HuaweiSmsConfig.class).getSignature())
                            .isEqualTo("sign");
                    assertThat(context.getBean(YunpianSmsConfig.class).getApiUrl())
                            .isEqualTo("http://localhost/yunpian");
                    assertThat(context.getBeansOfType(NotificationChannelProvider.class)
                                    .values())
                            .allSatisfy(provider ->
                                    assertThat(provider.isAvailable()).isTrue());
                });
    }

    @Test
    void perVendorSwitchesAndNamedOverridesDoNotDisableOtherVendors() {
        var custom = mock(NotificationChannelProvider.class);
        contexts.withBean("aliyunSmsProvider", NotificationChannelProvider.class, () -> custom)
                .withPropertyValues(
                        "loadup.gotone.binder.sms.huawei.enabled=false",
                        "loadup.gotone.binder.sms.yunpian.enabled=false")
                .run(context -> {
                    assertThat(context.getBeansOfType(NotificationChannelProvider.class))
                            .containsOnlyKeys("aliyunSmsProvider");
                    assertThat(context.getBean("aliyunSmsProvider")).isSameAs(custom);
                });
        contexts.withPropertyValues("loadup.gotone.binder.sms.aliyun.enabled=false")
                .run(context -> assertThat(context.getBeansOfType(NotificationChannelProvider.class))
                        .containsOnlyKeys("huaweiSmsProvider", "yunpianSmsProvider"));
    }
}
