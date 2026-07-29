package io.github.loadup.components.dfs.s3.autoconfig;

/*-
 * #%L
 * Loadup Dfs Binder S3
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

import io.github.loadup.components.dfs.DfsProvider;
import io.github.loadup.components.dfs.s3.S3DfsConfig;
import io.github.loadup.components.dfs.s3.S3DfsProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;

@AutoConfiguration
@ConditionalOnClass(S3Client.class)
@ConditionalOnProperty(prefix = "loadup.dfs", name = "binder-type", havingValue = "s3")
@EnableConfigurationProperties(S3DfsConfig.class)
public class S3DfsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public DfsProvider s3DfsProvider(S3DfsConfig config) {
        return new S3DfsProvider(config);
    }
}
