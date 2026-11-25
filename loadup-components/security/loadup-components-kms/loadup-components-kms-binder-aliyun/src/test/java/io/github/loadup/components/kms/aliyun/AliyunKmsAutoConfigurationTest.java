/*
 * Copyright (C) 2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.kms.aliyun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.aliyun.kms20160120.Client;
import io.github.loadup.components.kms.KmsKeyManager;
import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.kms.aliyun.autoconfigure.AliyunKmsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class AliyunKmsAutoConfigurationTest {
    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AliyunKmsAutoConfiguration.class));

    @Test
    void aliasOnlyBinderAssemblesWithoutDataSourceOrCloudCalls() {
        var client = mock(Client.class);
        runner.withPropertyValues(
                        "loadup.kms.enabled=true", "loadup.kms.binder=aliyun", "loadup.kms.management-enabled=true")
                .withBean(Client.class, () -> client)
                .run(context -> assertThat(context)
                        .hasSingleBean(AliyunKmsTemplate.class)
                        .hasSingleBean(KmsTemplate.class)
                        .doesNotHaveBean(KmsKeyManager.class));
        verifyNoInteractions(client);
    }

    @Test
    void accessKeyModeBindsAndCreatesTheSdkClient() {
        runner.withPropertyValues(
                        "loadup.kms.enabled=true",
                        "loadup.kms.binder=aliyun",
                        "loadup.kms.aliyun.region-id=cn-hangzhou",
                        "loadup.kms.aliyun.credentials.type=access-key",
                        "ALIBABA_CLOUD_ACCESS_KEY_ID=synthetic-id",
                        "ALIBABA_CLOUD_ACCESS_KEY_SECRET=synthetic-secret")
                .run(context -> assertThat(context).hasSingleBean(AliyunKmsTemplate.class));
    }

    @Test
    void disabledAndUnselectedBinderSkipConfiguration() {
        runner.withPropertyValues("loadup.kms.binder=aliyun")
                .run(context -> assertThat(context).doesNotHaveBean(KmsTemplate.class));
        runner.withPropertyValues("loadup.kms.enabled=true", "loadup.kms.binder=local")
                .run(context -> assertThat(context).doesNotHaveBean(KmsTemplate.class));
    }

    @Test
    void callerTemplateOverridesAliyunBinderWithoutItsConfiguration() {
        var provided = mock(KmsTemplate.class);
        runner.withPropertyValues("loadup.kms.enabled=true", "loadup.kms.binder=aliyun")
                .withBean(KmsTemplate.class, () -> provided)
                .run(context -> {
                    assertThat(context).hasSingleBean(KmsTemplate.class).doesNotHaveBean(AliyunKmsTemplate.class);
                    assertThat(context.getBean(KmsTemplate.class)).isSameAs(provided);
                });
    }
}
