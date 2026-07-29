package io.github.loadup.components.dfs.autoconfig;

/*-
 * #%L
 * Loadup Dfs Components Api
 * %%
 * Copyright (C) 2025 - 2026 loadup_cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
