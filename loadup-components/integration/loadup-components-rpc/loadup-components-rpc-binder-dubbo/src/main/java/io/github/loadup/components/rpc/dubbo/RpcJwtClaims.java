/*
 * #%L
 * LoadUp Components RPC Binder Dubbo
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
package io.github.loadup.components.rpc.dubbo;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import org.springframework.security.oauth2.jwt.Jwt;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

final class RpcJwtClaims {
    private static final TypeReference<Map<String, Object>> MAP = new TypeReference<>() {};
    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private RpcJwtClaims() {}

    static Jwt parse(String token) {
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid access token");
        }
        Map<String, Object> headers;
        Map<String, Object> claims;
        try {
            headers = MAPPER.readValue(Base64.getUrlDecoder().decode(parts[0]), MAP);
            claims = MAPPER.readValue(Base64.getUrlDecoder().decode(parts[1]), MAP);
        } catch (RuntimeException invalid) {
            throw new IllegalArgumentException("Invalid access token", invalid);
        }
        Instant issuedAt = instant(claims.get("iat"));
        Instant expiresAt = instant(claims.get("exp"));
        if (expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Expired access token");
        }
        var builder = Jwt.withTokenValue(token)
                .headers(target -> target.putAll(headers))
                .claims(target -> target.putAll(claims))
                .expiresAt(expiresAt);
        if (issuedAt != null) {
            builder.issuedAt(issuedAt);
        }
        return builder.build();
    }

    private static Instant instant(Object value) {
        if (value instanceof Number number) {
            return Instant.ofEpochSecond(number.longValue());
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Instant.ofEpochSecond(Long.parseLong(text));
            } catch (NumberFormatException invalid) {
                throw new IllegalArgumentException("Invalid access token", invalid);
            }
        }
        return null;
    }
}
