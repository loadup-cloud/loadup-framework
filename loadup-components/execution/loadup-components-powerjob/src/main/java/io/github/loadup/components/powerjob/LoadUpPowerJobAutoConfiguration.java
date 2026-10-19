/*
 * #%L
 * LoadUp Components PowerJob
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
package io.github.loadup.components.powerjob;

import io.github.loadup.commons.error.CommonException;
import io.micrometer.observation.ObservationRegistry;
import java.util.Arrays;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import tech.powerjob.client.PowerJobClient;
import tech.powerjob.worker.autoconfigure.PowerJobProperties;

/** Adds client lifecycle and observations without replacing the official worker lifecycle. */
@AutoConfiguration(
        afterName = "org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration")
@EnableConfigurationProperties({PowerJobClientProperties.class, PowerJobProperties.class})
public class LoadUpPowerJobAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public PowerJobProcessorAspect powerJobProcessorAspect(
            org.springframework.beans.factory.ObjectProvider<ObservationRegistry> observations) {
        return new PowerJobProcessorAspect(observations.getIfAvailable(() -> ObservationRegistry.NOOP));
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(PowerJobClient.class)
    @ConditionalOnProperty(prefix = "powerjob.client", name = "enabled", havingValue = "true")
    public PowerJobClient powerJobClient(PowerJobClientProperties client, PowerJobProperties properties) {
        String address = value(client.getServerAddress(), properties.getWorker().getServerAddress());
        String appName = value(client.getAppName(), properties.getWorker().getAppName());
        String password = client.getPassword();
        if (address == null || appName == null || password == null || password.isBlank())
            throw new CommonException(PowerJobResultCodeEnum.INVALID_CLIENT_CONFIGURATION);
        var addresses = Arrays.stream(address.split(",", -1)).map(String::trim).toList();
        if (addresses.stream().anyMatch(item -> item.isBlank() || item.contains("://")))
            throw new CommonException(PowerJobResultCodeEnum.INVALID_CLIENT_CONFIGURATION);
        return new PowerJobClient(addresses, appName, password);
    }

    private static String value(String supplied, String fallback) {
        String value = supplied == null || supplied.isBlank() ? fallback : supplied;
        return value == null || value.isBlank() ? null : value.trim();
    }
}
