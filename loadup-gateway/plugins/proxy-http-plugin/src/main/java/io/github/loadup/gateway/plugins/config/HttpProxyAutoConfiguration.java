package io.github.loadup.gateway.plugins.config;

/*-
 * #%L
 * Proxy HTTP Plugin
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

import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.plugins.HttpProxyProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class HttpProxyAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(HttpProxyAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public HttpProxyProcessor httpProxyProcessor(GatewayProperties props) {
        return new HttpProxyProcessor(props);
    }
}
