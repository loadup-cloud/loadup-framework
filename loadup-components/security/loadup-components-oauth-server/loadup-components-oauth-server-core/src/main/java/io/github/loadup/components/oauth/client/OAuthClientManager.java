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

import static io.github.loadup.commons.enums.CommonResultCodeEnum.CONFLICT;
import static io.github.loadup.commons.enums.CommonResultCodeEnum.NOT_FOUND;
import static io.github.loadup.commons.enums.CommonResultCodeEnum.PARAM_ILLEGAL;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.oauth.OAuthClientSettings;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.transaction.support.TransactionOperations;

/** Confidential-client lifecycle management. Invoke only from an authorized management boundary. */
public final class OAuthClientManager {
    private final RegisteredClientRepository storage;
    private final JdbcOperations jdbc;
    private final TransactionOperations transactions;
    private final Clock clock;
    private final List<OAuthClientPolicy> policies;
    private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    public OAuthClientManager(
            LifecycleRegisteredClientRepository clients,
            JdbcOperations jdbc,
            TransactionOperations transactions,
            Clock clock,
            List<OAuthClientPolicy> policies) {
        this.storage = Objects.requireNonNull(clients).storage();
        this.jdbc = Objects.requireNonNull(jdbc);
        this.transactions = Objects.requireNonNull(transactions);
        this.clock = Objects.requireNonNull(clock);
        this.policies = List.copyOf(policies);
    }

    /** Template uses standard registration fields; plaintext or encoded secrets are not accepted. */
    public IssuedClientCredential create(RegisteredClient template, Instant secretExpiresAt) {
        if (template == null || template.getClientSecret() != null || template.getClientSecretExpiresAt() != null) {
            throw new CommonException(PARAM_ILLEGAL);
        }
        requireFuture(secretExpiresAt);
        String secret = generateSecret();
        var client = RegisteredClient.from(template)
                .clientIdIssuedAt(clock.instant())
                .clientSecret(encoder.encode(secret))
                .clientSecretExpiresAt(secretExpiresAt)
                .clientSettings(ClientSettings.withSettings(
                                enabledSettings(template, true).getSettings())
                        .setting(LifecycleRegisteredClientRepository.REVISION, 1L)
                        .build())
                .build();
        validate(client);
        try {
            transactions.executeWithoutResult(status -> {
                // There is no row to lock yet. Serialize only these two identifiers so the
                // standard repository's save/upsert cannot overwrite a concurrent creation.
                lockCreateIdentifier("client-id:" + client.getClientId());
                lockCreateIdentifier("registration-id:" + client.getId());
                if (storage.findById(client.getId()) != null || storage.findByClientId(client.getClientId()) != null) {
                    throw new CommonException(CONFLICT);
                }
                storage.save(client);
            });
        } catch (DataIntegrityViolationException conflict) {
            // Do not expose JDBC diagnostics or encoded credential values.
            throw new CommonException(CONFLICT);
        }
        return new IssuedClientCredential(client.getId(), client.getClientId(), secret, secretExpiresAt);
    }

    /** Read management metadata even after disable/expiry, without exposing the credential hash. */
    public RegisteredClient get(String registrationId) {
        var client = requireClient(registrationId);
        return RegisteredClient.from(client).clientSecret(null).build();
    }

    /** Registration identity and credential lifecycle fields are server-owned and preserved. */
    public void updateAuthorization(String registrationId, RegisteredClient desired) {
        if (desired == null) {
            throw new CommonException(PARAM_ILLEGAL);
        }
        mutate(
                registrationId,
                current -> {
                    if (!current.getId().equals(desired.getId())
                            || !current.getClientId().equals(desired.getClientId())) {
                        throw new CommonException(PARAM_ILLEGAL);
                    }
                    return RegisteredClient.from(current)
                            .clientName(desired.getClientName())
                            .clientAuthenticationMethods(methods -> {
                                methods.clear();
                                methods.addAll(desired.getClientAuthenticationMethods());
                            })
                            .authorizationGrantTypes(grants -> {
                                grants.clear();
                                grants.addAll(desired.getAuthorizationGrantTypes());
                            })
                            .redirectUris(uris -> {
                                uris.clear();
                                uris.addAll(desired.getRedirectUris());
                            })
                            .postLogoutRedirectUris(uris -> {
                                uris.clear();
                                uris.addAll(desired.getPostLogoutRedirectUris());
                            })
                            .scopes(scopes -> {
                                scopes.clear();
                                scopes.addAll(desired.getScopes());
                            })
                            .clientSettings(enabledSettings(
                                    desired,
                                    current.getClientSettings().getSetting(OAuthClientSettings.ENABLED) == null
                                            || Boolean.TRUE.equals(current.getClientSettings()
                                                    .getSetting(OAuthClientSettings.ENABLED))))
                            .tokenSettings(desired.getTokenSettings())
                            .build();
                },
                true);
    }

    public void disable(String registrationId) {
        mutate(
                registrationId,
                current -> RegisteredClient.from(current)
                        .clientSettings(enabledSettings(current, false))
                        .build(),
                false);
    }

    /** Sets the expiration instant; now or an earlier instant expires the credential immediately. */
    public void setCredentialExpiry(String registrationId, Instant expiresAt) {
        if (expiresAt == null) {
            throw new CommonException(PARAM_ILLEGAL);
        }
        mutate(
                registrationId,
                current -> RegisteredClient.from(current)
                        .clientSecretExpiresAt(expiresAt)
                        .build(),
                false);
    }

    /** Immediate replacement. Rotation never re-enables a disabled client. */
    public IssuedClientCredential rotateCredential(String registrationId, Instant expiresAt) {
        requireFuture(expiresAt);
        String secret = generateSecret();
        String hash = encoder.encode(secret);
        var updated = mutate(
                registrationId,
                current -> RegisteredClient.from(current)
                        .clientSecret(hash)
                        .clientSecretExpiresAt(expiresAt)
                        .build(),
                false);
        return new IssuedClientCredential(updated.getId(), updated.getClientId(), secret, expiresAt);
    }

    private void lockCreateIdentifier(String identifier) {
        jdbc.query(
                "select pg_advisory_xact_lock(hashtextextended(?, 0))",
                (row, index) -> Boolean.TRUE,
                "loadup-oauth:create:" + identifier);
    }

    private RegisteredClient mutate(String id, UnaryOperator<RegisteredClient> mutation, boolean validatePolicy) {
        requireText(id, 100);
        return transactions.execute(status -> {
            // A row lock serializes management changes across replicas, without a global registration lock.
            if (jdbc.queryForList("select id from oauth2_registered_client where id = ? for update", String.class, id)
                    .isEmpty()) {
                throw new CommonException(NOT_FOUND);
            }
            var current = requireClient(id);
            var changed = mutation.apply(current);
            var updated = RegisteredClient.from(changed)
                    .clientSettings(ClientSettings.withSettings(
                                    changed.getClientSettings().getSettings())
                            .setting(
                                    LifecycleRegisteredClientRepository.REVISION,
                                    Math.incrementExact(LifecycleRegisteredClientRepository.revision(current)))
                            .build())
                    .build();
            if (validatePolicy) {
                validate(updated);
            }
            storage.save(updated);
            return updated;
        });
    }

    private RegisteredClient requireClient(String id) {
        requireText(id, 100);
        var client = storage.findById(id);
        if (client == null) {
            throw new CommonException(NOT_FOUND);
        }
        return client;
    }

    private void validate(RegisteredClient client) {
        requireText(client.getId(), 100);
        requireText(client.getClientId(), 100);
        requireText(client.getClientName(), 200);
        if (client.getClientAuthenticationMethods().isEmpty()
                || client.getClientAuthenticationMethods().stream()
                        .anyMatch(method -> !ClientAuthenticationMethod.CLIENT_SECRET_BASIC.equals(method)
                                && !ClientAuthenticationMethod.CLIENT_SECRET_POST.equals(method))) {
            throw new CommonException(PARAM_ILLEGAL);
        }
        if (client.getTokenSettings().getAccessTokenTimeToLive() == null
                || client.getTokenSettings().getAccessTokenTimeToLive().isZero()
                || client.getTokenSettings().getAccessTokenTimeToLive().isNegative()
                || client.getTokenSettings().getRefreshTokenTimeToLive() == null
                || client.getTokenSettings().getRefreshTokenTimeToLive().isZero()
                || client.getTokenSettings().getRefreshTokenTimeToLive().isNegative()) {
            throw new CommonException(PARAM_ILLEGAL);
        }
        requireJoinedLength(
                client.getAuthorizationGrantTypes().stream()
                        .map(grant -> grant.getValue())
                        .toList(),
                1000);
        requireJoinedLength(client.getScopes(), 1000);
        requireJoinedLength(client.getRedirectUris(), 1000);
        requireJoinedLength(client.getPostLogoutRedirectUris(), 1000);
        // RFC 6749 scope-token excludes whitespace, double quote and backslash.
        for (var scope : client.getScopes()) {
            if (scope.isEmpty() || scope.chars().anyMatch(c -> c <= 32 || c >= 127 || c == 34 || c == 92)) {
                throw new CommonException(PARAM_ILLEGAL);
            }
        }
        for (var grant : client.getAuthorizationGrantTypes()) {
            requireText(grant.getValue(), 128);
        }
        for (var policy : policies) {
            policy.validate(client);
        }
    }

    private static void requireJoinedLength(Collection<String> values, int maximum) {
        // The standard JDBC repository uses comma-delimited columns for these sets.
        if (values.stream().anyMatch(value -> value.contains(","))
                || String.join(",", values).length() > maximum) {
            throw new CommonException(PARAM_ILLEGAL);
        }
    }

    private static void requireText(String value, int maximum) {
        if (value == null
                || value.isBlank()
                || !value.equals(value.trim())
                || value.length() > maximum
                || value.chars().anyMatch(Character::isISOControl)) {
            throw new CommonException(PARAM_ILLEGAL);
        }
    }

    private void requireFuture(Instant expiresAt) {
        if (expiresAt == null || !expiresAt.isAfter(clock.instant())) {
            throw new CommonException(PARAM_ILLEGAL);
        }
    }

    private static ClientSettings enabledSettings(RegisteredClient client, boolean enabled) {
        return ClientSettings.withSettings(client.getClientSettings().getSettings())
                .setting(OAuthClientSettings.ENABLED, enabled)
                .build();
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }
}
