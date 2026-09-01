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

import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * SPI for plugging a token verification backend into the gateway resource server.
 *
 * <p>The default implementation is the standard OAuth2/Nimbus binder
 * ({@link NimbusResourceServerBinder}). Alternative resource server integrations (e.g.
 * Sa-Token, opaque token introspection) implement this interface and expose a
 * {@link JwtDecoder} compatible with the gateway's claims contract.
 */
public interface ResourceServerBinder {

    /**
     * Unique binder type identifier (e.g. {@code nimbus}, {@code sa-token}).
     *
     * @return binder type
     */
    String getType();

    /**
     * Builds the {@link JwtDecoder} used by {@code BearerTokenAuthenticationFilter}.
     *
     * @return the configured decoder
     */
    JwtDecoder jwtDecoder();
}
