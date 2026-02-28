package io.github.loadup.components.signature.config;

/*-
 * #%L
 * LoadUp Components :: Signature
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

import io.github.loadup.components.signature.properties.SignatureProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * Signature 自动配置
 *
 * @author loadup
 */
@Slf4j
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "loadup.components.signature",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties(SignatureProperties.class)
@ComponentScan(basePackages = "io.github.loadup.components.signature")
public class SignatureAutoConfiguration {

    public SignatureAutoConfiguration(SignatureProperties properties) {
        log.info(
                "LoadUp Signature 组件已启用: defaultSignatureAlgorithm={}, defaultDigestAlgorithm={}",
                properties.getDefaultSignatureAlgorithm(),
                properties.getDefaultDigestAlgorithm());
    }
}
