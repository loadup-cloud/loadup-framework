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
package io.github.loadup.components.oauth.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.loadup.components.oauth.OAuthClientSettings;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.transaction.support.TransactionOperations;

class OAuthClientProtocolLifecycleTest {
    private static final Instant NOW = Instant.parse("2026-11-14T00:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final RegisteredClientRepository storage = mock(RegisteredClientRepository.class);
    private final LifecycleRegisteredClientRepository protocol = new LifecycleRegisteredClientRepository(
            storage, mock(JdbcOperations.class), TransactionOperations.withoutTransaction(), clock);

    private static RegisteredClient client(boolean enabled, Instant expiresAt, String hash) {
        return RegisteredClient.withId("client")
                .clientId("application")
                .clientSecret(hash)
                .clientSecretExpiresAt(expiresAt)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read")
                .clientSettings(ClientSettings.builder()
                        .setting(OAuthClientSettings.ENABLED, enabled)
                        .build())
                .build();
    }

    @Test
    void disabledAndExpiredCredentialsAreHiddenFromEveryProtocolLookup() {
        for (var value : new RegisteredClient[] {
            client(false, NOW.plusSeconds(60), "hash"),
            client(true, NOW, "hash"),
            client(true, NOW.minusSeconds(1), "hash")
        }) {
            when(storage.findById("client")).thenReturn(value);
            when(storage.findByClientId("application")).thenReturn(value);
            assertThat(protocol.findById("client")).isNull();
            assertThat(protocol.findByClientId("application")).isNull();
        }
        var active = client(true, NOW.plusSeconds(1), "hash");
        when(storage.findById("client")).thenReturn(active);
        assertThat(protocol.findById("client")).isSameAs(active);
    }

    @Test
    void standardRegistrationsWithoutLifecycleMetadataRemainUsable() {
        var standard = RegisteredClient.from(client(true, null, "hash"))
                .clientSettings(ClientSettings.builder().build())
                .build();
        when(storage.findById("client")).thenReturn(standard);
        assertThat(protocol.findById("client")).isSameAs(standard);
    }

    @Test
    void anAuthenticationPasswordUpgradeCannotOverwriteANewerManagementRevision() {
        var original = client(true, NOW.plusSeconds(60), "old");
        var disabled = RegisteredClient.from(original)
                .clientSettings(ClientSettings.builder()
                        .setting(OAuthClientSettings.ENABLED, false)
                        .setting(LifecycleRegisteredClientRepository.REVISION, 1L)
                        .build())
                .build();
        when(storage.findById("client")).thenReturn(disabled);
        var staleUpgrade = RegisteredClient.from(original)
                .clientSecret("upgraded-old-secret")
                .build();

        assertThatThrownBy(() -> protocol.save(staleUpgrade)).isInstanceOf(OAuth2AuthenticationException.class);
        verify(storage, never()).save(staleUpgrade);
    }

    @Test
    void tokenGenerationRechecksCurrentAuthorizationAndCredential() {
        var original = client(true, NOW.plusSeconds(60), "original-hash");
        var context = mock(JwtEncodingContext.class);
        when(context.getRegisteredClient()).thenReturn(original);
        when(context.getAuthorizationGrantType()).thenReturn(AuthorizationGrantType.CLIENT_CREDENTIALS);
        when(context.getAuthorizedScopes()).thenReturn(Set.of("read"));
        var validator = new OAuthClientTokenValidator(protocol, clock);
        when(storage.findById("client")).thenReturn(original);
        assertThat(validator.requireAuthorizedClient(context)).isSameAs(original);

        var reencoded = client(true, NOW.plusSeconds(60), "reencoded-same-credential");
        when(storage.findById("client")).thenReturn(reencoded);
        assertThat(validator.requireAuthorizedClient(context)).isSameAs(reencoded);

        for (var current : new RegisteredClient[] {
            client(false, NOW.plusSeconds(60), "original-hash"),
            client(true, NOW, "original-hash"),
            RegisteredClient.from(client(true, NOW.plusSeconds(60), "rotated-hash"))
                    .clientSettings(ClientSettings.builder()
                            .setting(OAuthClientSettings.ENABLED, true)
                            .setting(LifecycleRegisteredClientRepository.REVISION, 1L)
                            .build())
                    .build(),
            RegisteredClient.from(original)
                    .scopes(scopes -> {
                        scopes.clear();
                        scopes.add("other");
                    })
                    .build(),
            RegisteredClient.from(original)
                    .authorizationGrantTypes(grants -> {
                        grants.clear();
                        grants.add(AuthorizationGrantType.AUTHORIZATION_CODE);
                    })
                    .redirectUri("https://example.test/callback")
                    .build()
        }) {
            when(storage.findById("client")).thenReturn(current);
            assertThatThrownBy(() -> validator.requireAuthorizedClient(context))
                    .isInstanceOf(OAuth2AuthenticationException.class);
        }
    }
}
