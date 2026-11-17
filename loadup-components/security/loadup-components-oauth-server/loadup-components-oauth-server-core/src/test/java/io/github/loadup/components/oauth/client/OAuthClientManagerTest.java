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
import static io.github.loadup.commons.enums.CommonResultCodeEnum.PARAM_ILLEGAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.oauth.OAuthClientSettings;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

class OAuthClientManagerTest {
    private static final Instant NOW = Instant.parse("2026-11-14T00:00:00Z");
    private static final Instant FUTURE = NOW.plusSeconds(3600);
    private static final Pattern GENERATED_SECRET = Pattern.compile("[A-Za-z0-9_-]{43}");

    private Clock clock;
    private InMemoryRegisteredClientRepository storage;
    private LifecycleRegisteredClientRepository clients;
    private JdbcOperations jdbc;
    private TransactionOperations transactions;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZoneOffset.UTC);
        storage = new InMemoryRegisteredClientRepository(registeredClient("seed-id", "seed-client", "seed-hash"));
        jdbc = mock(JdbcOperations.class);
        when(jdbc.queryForList(anyString(), eq(String.class), any(Object[].class)))
                .thenReturn(List.of("seed-id"));
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(new SimpleTransactionStatus());
        transactions = new TransactionTemplate(transactionManager);
        clients = new LifecycleRegisteredClientRepository(storage, jdbc, transactions, clock);
    }

    @Test
    void createIssuesUrlSafeSecretStoresHashAndGetRedactsIt() {
        OAuthClientManager manager = manager(List.of());

        IssuedClientCredential issued = manager.create(template("new-id", "new-client"), FUTURE);

        assertEquals("new-id", issued.registrationId());
        assertEquals("new-client", issued.clientId());
        assertTrue(GENERATED_SECRET.matcher(issued.secret()).matches());
        assertFalse(issued.toString().contains(issued.secret()));
        assertTrue(issued.toString().contains("REDACTED"));

        RegisteredClient stored = storage.findById("new-id");
        assertNotNull(stored);
        assertTrue(stored.getClientSecret().startsWith("{bcrypt}"));
        assertNotEquals(issued.secret(), stored.getClientSecret());
        assertTrue(PasswordEncoderFactories.createDelegatingPasswordEncoder()
                .matches(issued.secret(), stored.getClientSecret()));
        assertEquals(FUTURE, stored.getClientSecretExpiresAt());

        RegisteredClient redacted = manager.get("new-id");
        assertNull(redacted.getClientSecret());
        assertFalse(redacted.toString().contains(stored.getClientSecret()));
    }

    @Test
    void updateAuthorizationPreservesIdentityCredentialIssueTimeAndDisabledState() {
        RegisteredClient original = registeredClient("managed-id", "managed-client", "{noop}stored-secret");
        original = RegisteredClient.from(original)
                .clientIdIssuedAt(NOW.minusSeconds(600))
                .clientSecretExpiresAt(FUTURE)
                .clientSettings(ClientSettings.builder()
                        .setting(OAuthClientSettings.ENABLED, false)
                        .build())
                .build();
        storage.save(original);
        OAuthClientManager manager = manager(List.of());

        RegisteredClient desired = RegisteredClient.from(original)
                .clientSecret("forged-secret")
                .clientIdIssuedAt(NOW)
                .clientSecretExpiresAt(NOW.plusSeconds(7200))
                .clientName("Updated client")
                .scopes(scopes -> {
                    scopes.clear();
                    scopes.add("updated");
                })
                .clientSettings(ClientSettings.builder()
                        .setting(OAuthClientSettings.ENABLED, true)
                        .build())
                .build();

        manager.updateAuthorization("managed-id", desired);

        RegisteredClient updated = storage.findById("managed-id");
        assertEquals("managed-id", updated.getId());
        assertEquals("managed-client", updated.getClientId());
        assertEquals("{noop}stored-secret", updated.getClientSecret());
        assertEquals(NOW.minusSeconds(600), updated.getClientIdIssuedAt());
        assertEquals(FUTURE, updated.getClientSecretExpiresAt());
        assertEquals("Updated client", updated.getClientName());
        assertEquals(List.of("updated"), updated.getScopes().stream().toList());
        assertEquals(Boolean.FALSE, updated.getClientSettings().getSetting(OAuthClientSettings.ENABLED));
    }

    @Test
    void disableExpiryAndRotationPreserveDisabledStateAndInvalidateOldCredential() {
        storage.save(registeredClient("managed-id", "managed-client", "{noop}old-secret"));
        OAuthClientManager manager = manager(List.of());
        manager.disable("managed-id");
        manager.setCredentialExpiry("managed-id", NOW.minusSeconds(1));
        assertNull(clients.findByClientId("managed-client"));

        IssuedClientCredential rotated = manager.rotateCredential("managed-id", FUTURE);

        RegisteredClient stored = storage.findById("managed-id");
        assertEquals(Boolean.FALSE, stored.getClientSettings().getSetting(OAuthClientSettings.ENABLED));
        assertEquals(FUTURE, stored.getClientSecretExpiresAt());
        assertNotEquals("{noop}old-secret", stored.getClientSecret());
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        assertFalse(encoder.matches("old-secret", stored.getClientSecret()));
        assertTrue(encoder.matches(rotated.secret(), stored.getClientSecret()));
        assertNull(clients.findByClientId("managed-client"));
    }

    @Test
    void settingCredentialExpiryMakesAnEnabledClientInactiveAtTheExpiryInstant() {
        storage.save(registeredClient("expiring-id", "expiring-client", "{noop}secret"));
        OAuthClientManager manager = manager(List.of());

        manager.setCredentialExpiry("expiring-id", NOW);

        assertEquals(NOW, storage.findById("expiring-id").getClientSecretExpiresAt());
        assertNull(clients.findByClientId("expiring-client"));
    }

    @Test
    void duplicateIdentifiersAndInvalidTemplatesDoNotCreateRecords() {
        OAuthClientManager manager = manager(List.of());

        assertEquals(
                CONFLICT,
                assertThrows(
                                CommonException.class,
                                () -> manager.create(template("seed-id", "different-client"), FUTURE))
                        .getResultCode());
        assertEquals(
                CONFLICT,
                assertThrows(
                                CommonException.class,
                                () -> manager.create(template("different-id", "seed-client"), FUTURE))
                        .getResultCode());
        assertEquals(
                PARAM_ILLEGAL,
                assertThrows(
                                CommonException.class,
                                () -> manager.create(template("invalid-secret", "invalid-client", "secret"), FUTURE))
                        .getResultCode());
        assertEquals(
                PARAM_ILLEGAL,
                assertThrows(
                                CommonException.class,
                                () -> manager.create(
                                        template("invalid-expiry", "invalid-expiry-client", null, NOW), FUTURE))
                        .getResultCode());
        assertEquals(
                PARAM_ILLEGAL,
                assertThrows(CommonException.class, () -> manager.create(template("expired", "expired-client"), NOW))
                        .getResultCode());

        assertNull(storage.findById("different-id"));
        assertNull(storage.findById("invalid-secret"));
        assertNull(storage.findById("invalid-expiry"));
        assertNull(storage.findById("expired"));
    }

    @Test
    void aCommaDelimitedScopeCannotChangeMeaningWhenStoredInTheStandardJdbcColumn() {
        var template = RegisteredClient.from(template("comma-id", "comma-client"))
                .scope("other,admin")
                .build();
        assertEquals(
                PARAM_ILLEGAL,
                assertThrows(CommonException.class, () -> manager(List.of()).create(template, FUTURE))
                        .getResultCode());
        assertNull(storage.findById("comma-id"));
    }

    @Test
    void rejectedPolicyLeavesNoRecord() {
        OAuthClientManager manager = manager(List.of(client -> {
            throw new IllegalStateException("rejected by policy");
        }));

        assertThrows(
                IllegalStateException.class, () -> manager.create(template("rejected-id", "rejected-client"), FUTURE));

        assertNull(storage.findById("rejected-id"));
    }

    private OAuthClientManager manager(List<OAuthClientPolicy> policies) {
        return new OAuthClientManager(clients, jdbc, transactions, clock, policies);
    }

    private static RegisteredClient template(String id, String clientId) {
        return template(id, clientId, null, null);
    }

    private static RegisteredClient template(String id, String clientId, String secret) {
        return template(id, clientId, secret, null);
    }

    private static RegisteredClient template(String id, String clientId, String secret, Instant secretExpiresAt) {
        var builder = RegisteredClient.withId(id)
                .clientId(clientId)
                .clientName("Test client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read")
                .clientSettings(ClientSettings.builder().build())
                .tokenSettings(TokenSettings.builder().build());
        if (secret != null) {
            builder.clientSecret(secret);
        }
        if (secretExpiresAt != null) {
            builder.clientSecretExpiresAt(secretExpiresAt);
        }
        return builder.build();
    }

    private static RegisteredClient registeredClient(String id, String clientId, String secret) {
        return RegisteredClient.withId(id)
                .clientId(clientId)
                .clientIdIssuedAt(NOW.minusSeconds(60))
                .clientSecret(secret)
                .clientSecretExpiresAt(FUTURE)
                .clientName("Seed client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("read")
                .clientSettings(ClientSettings.builder()
                        .setting(OAuthClientSettings.ENABLED, true)
                        .build())
                .tokenSettings(TokenSettings.builder().build())
                .build();
    }
}
