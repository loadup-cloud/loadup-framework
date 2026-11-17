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

import io.github.loadup.components.oauth.client.IssuedClientCredential;
import io.github.loadup.components.runtime.mapping.LoadUpMapStructConfig;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

@Mapper(config = LoadUpMapStructConfig.class)
public interface OAuthClientManagementConverter {
    @Mapping(target = "registrationId", expression = "java(value.registrationId())")
    @Mapping(target = "clientId", expression = "java(value.clientId())")
    @Mapping(target = "secret", expression = "java(value.secret())")
    @Mapping(target = "expiresAt", expression = "java(value.expiresAt())")
    OAuthClientCredentialResponse credential(IssuedClientCredential value);

    @Mapping(target = "clientName", source = "client.clientName")
    @Mapping(target = "scopes", source = "client.scopes")
    @Mapping(
            target = "audience",
            expression =
                    "java(client.getClientSettings().getSetting(io.github.loadup.components.oauth.OAuthClientSettings.AUDIENCE))")
    @Mapping(target = "attributes", source = "attributes")
    @Mapping(target = "accessTokenTtl", source = "client.tokenSettings.accessTokenTimeToLive")
    OAuthClientAuthorization authorization(RegisteredClient client, Map<String, String> attributes);

    @Mapping(target = "registrationId", source = "client.id")
    @Mapping(target = "clientId", source = "client.clientId")
    @Mapping(target = "authorization", source = "authorization")
    @Mapping(
            target = "enabled",
            expression =
                    "java(client.getClientSettings().getSetting(io.github.loadup.components.oauth.OAuthClientSettings.ENABLED)"
                            + " == null ||"
                            + " Boolean.TRUE.equals(client.getClientSettings().getSetting(io.github.loadup.components.oauth.OAuthClientSettings.ENABLED)))")
    @Mapping(target = "expiresAt", source = "client.clientSecretExpiresAt")
    OAuthClientDetails detail(RegisteredClient client, OAuthClientAuthorization authorization);
}
