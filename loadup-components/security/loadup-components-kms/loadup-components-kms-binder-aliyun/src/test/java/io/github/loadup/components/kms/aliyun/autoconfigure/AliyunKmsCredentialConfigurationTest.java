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
package io.github.loadup.components.kms.aliyun.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.kms.aliyun.config.AliyunKmsProperties.CredentialType;
import io.github.loadup.components.kms.aliyun.config.AliyunKmsProperties.Credentials;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class AliyunKmsCredentialConfigurationTest {
    @Test
    void ecsRamRoleUsesImdsV2WithoutAccessKeys() {
        var environment = new MockEnvironment().withProperty("ALIBABA_CLOUD_ACCESS_KEY_ID", "irrelevant-ak");
        var config = AliyunKmsAutoConfiguration.credentialConfig(
                new Credentials(CredentialType.ECS_RAM_ROLE, "loadup-kms"), environment);

        assertThat(config.getType()).isEqualTo("ecs_ram_role");
        assertThat(config.getRoleName()).isEqualTo("loadup-kms");
        assertThat(config.getDisableIMDSv1()).isTrue();
        assertThat(config.getAccessKeyId()).isNull();
        assertThat(config.getAccessKeySecret()).isNull();
    }

    @Test
    void accessKeyReadsBothValuesFromTheEnvironment() {
        var environment = new MockEnvironment()
                .withProperty("ALIBABA_CLOUD_ACCESS_KEY_ID", "synthetic-id")
                .withProperty("ALIBABA_CLOUD_ACCESS_KEY_SECRET", "synthetic-secret");
        var config = AliyunKmsAutoConfiguration.credentialConfig(
                new Credentials(CredentialType.ACCESS_KEY, null), environment);

        assertThat(config.getType()).isEqualTo("access_key");
        assertThat(config.getAccessKeyId()).isEqualTo("synthetic-id");
        assertThat(config.getAccessKeySecret()).isEqualTo("synthetic-secret");
    }

    @Test
    void missingCredentialsFailWithoutExposingTheOtherValue() {
        var environment = new MockEnvironment().withProperty("ALIBABA_CLOUD_ACCESS_KEY_ID", "synthetic-id");

        assertThatThrownBy(() -> AliyunKmsAutoConfiguration.credentialConfig(
                        new Credentials(CredentialType.ACCESS_KEY, null), environment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ALIBABA_CLOUD_ACCESS_KEY_SECRET")
                .hasMessageNotContaining("synthetic-id");
        assertThatThrownBy(() -> AliyunKmsAutoConfiguration.credentialConfig(null, environment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("loadup.kms.aliyun.credentials.type");
    }
}
