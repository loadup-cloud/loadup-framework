package io.github.loadup.components.configcenter.nacos.autoconfig;

/*-
 * #%L
 * LoadUp ConfigCenter Binder Nacos
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

import com.alibaba.nacos.api.NacosFactory;
import io.github.loadup.components.configcenter.ConfigCenterProvider;
import io.github.loadup.components.configcenter.nacos.NacosConfigCenterConfig;
import io.github.loadup.components.configcenter.nacos.NacosConfigCenterProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(NacosFactory.class)
@ConditionalOnProperty(prefix = "loadup.configcenter", name = "binder-type", havingValue = "nacos")
@EnableConfigurationProperties(NacosConfigCenterConfig.class)
public class NacosConfigCenterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConfigCenterProvider nacosConfigCenterProvider(NacosConfigCenterConfig config) {
        return new NacosConfigCenterProvider(config);
    }
}
