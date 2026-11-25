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

import com.aliyun.kms20160120.Client;
import com.aliyun.teaopenapi.models.Config;
import io.github.loadup.components.kms.KmsSecretManager;
import io.github.loadup.components.kms.KmsTemplate;
import io.github.loadup.components.kms.aliyun.AliyunKmsSecretManager;
import io.github.loadup.components.kms.aliyun.AliyunKmsTemplate;
import io.github.loadup.components.kms.aliyun.config.AliyunKmsProperties;
import io.github.loadup.components.kms.aliyun.config.AliyunKmsProperties.Credentials;
import java.util.Objects;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.kms", name = "enabled", havingValue = "true")
@ConditionalOnProperty(prefix = "loadup.kms", name = "binder", havingValue = "aliyun")
@EnableConfigurationProperties(AliyunKmsProperties.class)
public class AliyunKmsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(KmsTemplate.class)
    AliyunKmsTemplate aliyunKmsTemplate(Client client, AliyunKmsProperties properties) {
        return new AliyunKmsTemplate(client, properties);
    }

    @Bean
    @ConditionalOnMissingBean(KmsSecretManager.class)
    @ConditionalOnProperty(prefix = "loadup.kms", name = "secrets-enabled", havingValue = "true")
    KmsSecretManager aliyunKmsSecretManager(Client client, AliyunKmsProperties properties) {
        return new AliyunKmsSecretManager(client, properties);
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean(Client.class)
    Client aliyunKmsClient(AliyunKmsProperties properties, Environment environment) {
        String region = properties.regionId();
        if (region == null || !region.matches("[a-z0-9]+(?:-[a-z0-9]+)+")) {
            throw new IllegalArgumentException("A valid loadup.kms.aliyun.region-id is required");
        }
        String publicEndpoint = "kms." + region + ".aliyuncs.com";
        String vpcEndpoint = "kms-vpc." + region + ".aliyuncs.com";
        String endpoint = properties.endpoint() == null ? publicEndpoint : properties.endpoint();
        if (!Objects.equals(endpoint, publicEndpoint) && !Objects.equals(endpoint, vpcEndpoint)) {
            throw new IllegalArgumentException("Aliyun KMS requires a shared gateway in the configured region");
        }
        var credentials = credentialConfig(properties.credentials(), environment);
        try {
            return new Client(new Config()
                    .setProtocol("https")
                    .setRegionId(region)
                    .setEndpoint(endpoint)
                    .setCredential(new com.aliyun.credentials.Client(credentials)));
        } catch (Exception failure) {
            throw new IllegalStateException("Cannot initialize the Aliyun KMS SDK client");
        }
    }

    static com.aliyun.credentials.models.Config credentialConfig(Credentials credentials, Environment environment) {
        if (credentials == null || credentials.type() == null) {
            throw new IllegalArgumentException("loadup.kms.aliyun.credentials.type is required");
        }
        var config = new com.aliyun.credentials.models.Config();
        return switch (credentials.type()) {
            case ECS_RAM_ROLE ->
                config.setType("ecs_ram_role")
                        .setRoleName(credentials.roleName())
                        .setDisableIMDSv1(true);
            case ACCESS_KEY ->
                config.setType("access_key")
                        .setAccessKeyId(requiredAccessKeyProperty(environment, "ALIBABA_CLOUD_ACCESS_KEY_ID"))
                        .setAccessKeySecret(requiredAccessKeyProperty(environment, "ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
        };
    }

    private static String requiredAccessKeyProperty(Environment environment, String name) {
        String value = environment.getProperty(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Required Aliyun KMS credential property is missing: " + name);
        }
        return value;
    }
}
