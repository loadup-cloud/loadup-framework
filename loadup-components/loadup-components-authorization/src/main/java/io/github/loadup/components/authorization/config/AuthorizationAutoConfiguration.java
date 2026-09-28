package io.github.loadup.components.authorization.config;

/*-
 * #%L
 * LoadUp Components Authorization
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

import io.github.loadup.components.authorization.AuthorizationProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Auto-configuration for LoadUp Authorization.
 *
 * <p>The component is a thin integration on top of Spring Security: it enables method-level
 * security ({@code @PreAuthorize}). HTTP authentication and request authorization belong to the
 * resource-server or application-owned security chain.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.security.method-security", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableMethodSecurity
@EnableConfigurationProperties(AuthorizationProperties.class)
public class AuthorizationAutoConfiguration {}
