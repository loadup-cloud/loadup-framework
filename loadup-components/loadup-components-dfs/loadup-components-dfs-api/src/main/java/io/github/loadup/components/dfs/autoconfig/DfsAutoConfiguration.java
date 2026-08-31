package io.github.loadup.components.dfs.autoconfig;

/*-
 * #%L
 * Loadup Dfs Components Api
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

import io.github.loadup.components.dfs.DefaultDfsTemplate;
import io.github.loadup.components.dfs.DfsProperties;
import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.DfsTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnSingleCandidate(DfsProvider.class)
@EnableConfigurationProperties(DfsProperties.class)
public class DfsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(DfsTemplate.class)
    public DfsTemplate dfsTemplate(DfsProvider provider) {
        return new DefaultDfsTemplate(provider);
    }
}
