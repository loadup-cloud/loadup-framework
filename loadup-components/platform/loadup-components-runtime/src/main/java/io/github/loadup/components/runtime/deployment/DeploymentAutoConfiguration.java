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
package io.github.loadup.components.runtime.deployment;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Registers the immutable deployment identity for this process.
 *
 * <p>A missing or malformed {@code loadup.deployment.code} fails context startup; there is no
 * default deployment and no fallback to a request value.
 */
@AutoConfiguration
@EnableConfigurationProperties(DeploymentProperties.class)
public class DeploymentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DeploymentIdentity deploymentIdentity(DeploymentProperties properties) {
        return new DeploymentIdentity(properties.getCode());
    }
}
