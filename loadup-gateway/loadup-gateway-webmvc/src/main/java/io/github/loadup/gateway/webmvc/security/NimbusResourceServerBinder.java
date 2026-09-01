package io.github.loadup.gateway.webmvc.security;

/*-
 * #%L
 * Loadup Gateway WebMVC Engine
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

import io.github.loadup.gateway.facade.config.GatewayProperties;
import java.nio.charset.StandardCharsets;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Default standard OAuth2/Nimbus resource server binder.
 *
 * <p>Decoder selection:
 * <ol>
 *   <li>{@code loadup.gateway.security.jwk-set-uri} — Nimbus {@code withJwkSetUri};</li>
 *   <li>{@code loadup.gateway.security.issuer-uri} — Nimbus issuer discovery
 *       ({@code withIssuerLocation});</li>
 *   <li>{@code loadup.gateway.security.secret} — shared-secret HS256 (single-node default).</li>
 * </ol>
 */
public final class NimbusResourceServerBinder implements ResourceServerBinder {

    private final JwtDecoder jwtDecoder;

    public NimbusResourceServerBinder(GatewayProperties properties) {
        this.jwtDecoder = buildDecoder(properties);
    }

    @Override
    public String getType() {
        return "nimbus";
    }

    @Override
    public JwtDecoder jwtDecoder() {
        return jwtDecoder;
    }

    private static JwtDecoder buildDecoder(GatewayProperties properties) {
        GatewayProperties.SecurityConfig security = properties.getSecurity();
        if (StringUtils.isNotBlank(security.getJwkSetUri())) {
            return NimbusJwtDecoder.withJwkSetUri(security.getJwkSetUri()).build();
        }
        if (StringUtils.isNotBlank(security.getIssuerUri())) {
            return NimbusJwtDecoder.withIssuerLocation(security.getIssuerUri()).build();
        }

        String secret = security.getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "loadup.gateway.security.secret must be at least 32 bytes for HS256 JWT verification");
        }
        javax.crypto.SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
