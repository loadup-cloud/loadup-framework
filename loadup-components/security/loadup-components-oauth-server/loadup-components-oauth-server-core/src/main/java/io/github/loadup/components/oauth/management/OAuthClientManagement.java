/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.oauth.management;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.oauth.OAuthClientSettings;
import io.github.loadup.components.oauth.client.OAuthClientManager;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

/** Typed management projection; lifecycle and concurrency guarantees remain in OAuthClientManager. */
public final class OAuthClientManagement {
    private final OAuthClientManager manager;
    private final OAuthClientManagementConverter converter;

    public OAuthClientManagement(OAuthClientManager manager, OAuthClientManagementConverter converter) {
        this.manager = Objects.requireNonNull(manager);
        this.converter = Objects.requireNonNull(converter);
    }

    public OAuthClientCredentialResponse create(String id, OAuthClientCreateRequest request) {
        requireId(id);
        if (request == null
                || request.clientId() == null
                || request.clientId().isBlank()
                || request.clientId().length() > 100
                || !request.clientId().equals(request.clientId().trim())
                || request.clientId().chars().anyMatch(Character::isISOControl)) {
            throw invalid();
        }
        var template = apply(
                        RegisteredClient.withId(id)
                                .clientId(request.clientId())
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS),
                        request.authorization())
                .build();
        return converter.credential(manager.create(template, request.expiresAt()));
    }

    public OAuthClientDetails detail(String id) {
        requireId(id);
        var client = manager.get(id);
        Map<String, String> attributes = new LinkedHashMap<>();
        client.getClientSettings().getSettings().forEach((key, value) -> {
            if (!reserved(key) && value instanceof String text) {
                attributes.put(key, text);
            }
        });
        return converter.detail(client, converter.authorization(client, Map.copyOf(attributes)));
    }

    public void updateAuthorization(String id, OAuthClientAuthorization request) {
        requireId(id);
        var current = manager.get(id);
        manager.updateAuthorization(
                id, apply(RegisteredClient.from(current), request).build());
    }

    public void disable(String id) {
        requireId(id);
        manager.disable(id);
    }

    public void setCredentialExpiry(String id, OAuthClientExpiryRequest request) {
        requireId(id);
        if (request == null) {
            throw invalid();
        }
        manager.setCredentialExpiry(id, request.expiresAt());
    }

    public OAuthClientCredentialResponse rotateCredential(String id, OAuthClientExpiryRequest request) {
        requireId(id);
        if (request == null) {
            throw invalid();
        }
        return converter.credential(manager.rotateCredential(id, request.expiresAt()));
    }

    private static RegisteredClient.Builder apply(RegisteredClient.Builder builder, OAuthClientAuthorization request) {
        if (request == null
                || request.clientName() == null
                || request.clientName().isBlank()
                || request.clientName().length() > 200
                || !request.clientName().equals(request.clientName().trim())
                || request.clientName().chars().anyMatch(Character::isISOControl)
                || request.scopes() == null
                || request.scopes().isEmpty()
                || request.scopes().size() > 100
                || request.scopes().stream().anyMatch(scope -> scope == null || scope.isBlank() || scope.length() > 128)
                || request.attributes() == null
                || request.audience() == null
                || request.audience().isBlank()
                || request.audience().length() > 128
                || request.accessTokenTtl() == null
                || request.accessTokenTtl().isZero()
                || request.accessTokenTtl().isNegative()
                || request.attributes().size() > 8) {
            throw invalid();
        }
        var settings = ClientSettings.builder().setting(OAuthClientSettings.AUDIENCE, request.audience());
        request.attributes().forEach((key, value) -> {
            if (key == null
                    || reserved(key)
                    || !key.matches("[A-Za-z0-9_.-]{1,100}")
                    || value == null
                    || value.isBlank()
                    || value.length() > 256
                    || value.chars().anyMatch(Character::isISOControl)) {
                throw invalid();
            }
            settings.setting(key, value);
        });
        return builder.clientName(request.clientName())
                .scopes(scopes -> {
                    scopes.clear();
                    scopes.addAll(request.scopes());
                })
                .clientSettings(settings.build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(request.accessTokenTtl())
                        .build());
    }

    private static boolean reserved(String key) {
        return key.startsWith("settings.") || key.startsWith("loadup.");
    }

    public static void requireId(String id) {
        if (id == null || id.equals(".") || id.equals("..") || !id.matches("[A-Za-z0-9_.-]{1,100}")) {
            throw invalid();
        }
    }

    private static CommonException invalid() {
        return new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
    }
}
