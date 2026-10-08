package io.github.loadup.components.authorization.config;

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
@ConditionalOnProperty(
        prefix = "loadup.security.method-security",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
@EnableMethodSecurity
@EnableConfigurationProperties(AuthorizationProperties.class)
public class AuthorizationAutoConfiguration {}
