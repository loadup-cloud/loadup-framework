package io.github.loadup.components.configcenter.local.autoconfig;

/*-
 * #%L
 * LoadUp ConfigCenter Binder Local
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
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

import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.autoconfig.ConfigCenterAutoConfiguration;
import io.github.loadup.components.configcenter.local.LocalConfigCenterProperties;
import io.github.loadup.components.configcenter.local.LocalConfigCenterProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = ConfigCenterAutoConfiguration.class)
@ConditionalOnProperty(
        prefix = "loadup.configcenter",
        name = "binder-type",
        havingValue = "local",
        matchIfMissing = true)
@EnableConfigurationProperties(LocalConfigCenterProperties.class)
public class LocalConfigCenterAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ConfigCenterProvider localConfigCenterProvider() {
        return new LocalConfigCenterProvider();
    }
}
