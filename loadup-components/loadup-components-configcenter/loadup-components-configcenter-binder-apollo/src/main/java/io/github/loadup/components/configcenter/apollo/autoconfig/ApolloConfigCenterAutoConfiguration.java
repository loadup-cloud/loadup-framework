package io.github.loadup.components.configcenter.apollo.autoconfig;

/*-
 * #%L
 * LoadUp ConfigCenter Binder Apollo
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

import com.ctrip.framework.apollo.ConfigService;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.apollo.ApolloConfigCenterConfig;
import io.github.loadup.components.configcenter.apollo.ApolloConfigCenterProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ConfigService.class)
@ConditionalOnProperty(prefix = "loadup.configcenter", name = "binder-type", havingValue = "apollo")
@EnableConfigurationProperties(ApolloConfigCenterConfig.class)
public class ApolloConfigCenterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConfigCenterProvider apolloConfigCenterProvider(ApolloConfigCenterConfig config) {
        return new ApolloConfigCenterProvider(config);
    }
}
