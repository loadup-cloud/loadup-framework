package io.github.loadup.components.authserver.keycloak;

/*-
 * #%L
 * LoadUp Components AuthServer Binder Keycloak
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

import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Keycloak issuer-only binder.
 *
 * <p>Keycloak is used exclusively as an external issuer: this binder assembles a standard
 * {@link NimbusJwtDecoder} from the configured {@code issuer} or {@code jwk-set-uri} so the
 * application (and the embedded gateway) can validate Keycloak-issued tokens without any
 * Keycloak admin API integration.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "loadup.components.authserver", name = "binder-type", havingValue = "keycloak")
@EnableConfigurationProperties(LoadUpAuthServerProperties.class)
public class KeycloakAuthServerAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(KeycloakAuthServerAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    public JwtDecoder keycloakJwtDecoder(LoadUpAuthServerProperties properties) {
        String jwkSetUri = properties.getJwkSetUri();
        if (StringUtils.isNotBlank(jwkSetUri)) {
            log.info("Keycloak binder: verifying tokens against JWK set {}", jwkSetUri);
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        }
        String issuer = properties.getIssuer();
        if (StringUtils.isNotBlank(issuer)) {
            log.info("Keycloak binder: verifying tokens against issuer {}", issuer);
            return NimbusJwtDecoder.withIssuerLocation(issuer).build();
        }
        throw new IllegalStateException(
                "loadup.components.authserver.jwk-set-uri or issuer is required for the keycloak binder");
    }
}
