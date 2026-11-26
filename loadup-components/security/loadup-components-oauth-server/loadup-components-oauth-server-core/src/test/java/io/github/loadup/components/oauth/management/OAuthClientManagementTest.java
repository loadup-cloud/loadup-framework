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

import static io.github.loadup.commons.enums.CommonResultCodeEnum.PARAM_ILLEGAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.components.oauth.OAuthClientSettings;
import io.github.loadup.components.oauth.client.IssuedClientCredential;
import io.github.loadup.components.oauth.client.OAuthClientManager;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

class OAuthClientManagementTest {
    private static final String REGISTRATION_ID = "billing-service";
    private static final String CLIENT_ID = "billing-client";
    private static final String SECRET = "issued-secret-value";
    private static final String STORED_HASH = "{bcrypt}stored-credential-hash";
    private static final Instant ISSUED_AT = Instant.parse("2026-11-13T00:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2027-11-13T00:00:00Z");

    private OAuthClientManager manager;
    private OAuthClientManagement management;
    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void setUp() {
        manager = mock(OAuthClientManager.class);
        context = new AnnotationConfigApplicationContext(OAuthClientManagementConverterImpl.class);
        management = new OAuthClientManagement(manager, context.getBean(OAuthClientManagementConverter.class));
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void createMapsTypedRequestToClientCredentialsRegistrationAndReturnsIssuedCredentialOnly() {
        IssuedClientCredential issued = mock(IssuedClientCredential.class);
        when(issued.registrationId()).thenReturn(REGISTRATION_ID);
        when(issued.clientId()).thenReturn(CLIENT_ID);
        when(issued.secret()).thenReturn(SECRET);
        when(issued.expiresAt()).thenReturn(EXPIRES_AT);
        when(manager.create(any(RegisteredClient.class), eq(EXPIRES_AT))).thenReturn(issued);
        var request = new OAuthClientCreateRequest(
                CLIENT_ID,
                authorization(
                        "Billing integration",
                        Set.of("payments:read", "payments:write"),
                        "payments-api",
                        Map.of("application.owner", "billing")),
                EXPIRES_AT);

        OAuthClientCredentialResponse response = management.create(REGISTRATION_ID, request);

        ArgumentCaptor<RegisteredClient> registration = ArgumentCaptor.forClass(RegisteredClient.class);
        verify(manager).create(registration.capture(), eq(EXPIRES_AT));
        RegisteredClient template = registration.getValue();
        assertEquals(REGISTRATION_ID, template.getId());
        assertEquals(CLIENT_ID, template.getClientId());
        assertEquals(Set.of(ClientAuthenticationMethod.CLIENT_SECRET_BASIC), template.getClientAuthenticationMethods());
        assertEquals(Set.of(AuthorizationGrantType.CLIENT_CREDENTIALS), template.getAuthorizationGrantTypes());
        assertEquals(Set.of("payments:read", "payments:write"), template.getScopes());
        assertEquals("Billing integration", template.getClientName());
        assertEquals("payments-api", template.getClientSettings().getSetting(OAuthClientSettings.AUDIENCE));
        assertEquals("billing", template.getClientSettings().getSetting("application.owner"));
        assertEquals(Duration.ofMinutes(10), template.getTokenSettings().getAccessTokenTimeToLive());
        assertNull(template.getClientSecret());
        assertNull(template.getClientSecretExpiresAt());

        assertEquals(REGISTRATION_ID, response.registrationId());
        assertEquals(CLIENT_ID, response.clientId());
        assertEquals(SECRET, response.secret());
        assertEquals(EXPIRES_AT, response.expiresAt());
        assertFalse(response.toString().contains(SECRET));
    }

    @Test
    void updateAuthorizationPassesStandardModelWithStableIdentifiersAndReplacementScopes() {
        RegisteredClient current =
                RegisteredClient.from(registeredClient()).clientSecret(null).build();
        when(manager.get(REGISTRATION_ID)).thenReturn(current);
        var authorization = authorization(
                "Updated billing integration",
                Set.of("payments:admin"),
                "payments-api-v2",
                Map.of("application.owner", "finance"));

        management.updateAuthorization(REGISTRATION_ID, authorization);

        ArgumentCaptor<RegisteredClient> desired = ArgumentCaptor.forClass(RegisteredClient.class);
        verify(manager).updateAuthorization(eq(REGISTRATION_ID), desired.capture());
        RegisteredClient updated = desired.getValue();
        assertEquals(REGISTRATION_ID, updated.getId());
        assertEquals(CLIENT_ID, updated.getClientId());
        assertNull(updated.getClientSecret());
        assertEquals(ISSUED_AT, updated.getClientIdIssuedAt());
        assertEquals("Updated billing integration", updated.getClientName());
        assertEquals(Set.of("payments:admin"), updated.getScopes());
        assertEquals("payments-api-v2", updated.getClientSettings().getSetting(OAuthClientSettings.AUDIENCE));
        assertEquals("finance", updated.getClientSettings().getSetting("application.owner"));
    }

    @Test
    void detailExcludesStoredCredentialAndReservedSettings() {
        when(manager.get(REGISTRATION_ID)).thenReturn(registeredClientWithReservedSettings());

        OAuthClientDetails details = management.detail(REGISTRATION_ID);

        assertEquals(REGISTRATION_ID, details.registrationId());
        assertEquals(CLIENT_ID, details.clientId());
        assertEquals("payments-api", details.authorization().audience());
        assertEquals(
                Map.of("application.owner", "billing"), details.authorization().attributes());
        assertEquals(Duration.ofMinutes(10), details.authorization().accessTokenTtl());
        assertFalse(details.enabled());
        assertFalse(details.toString().contains(STORED_HASH));
        assertFalse(details.authorization().attributes().containsKey("settings.secret"));
        assertFalse(details.authorization().attributes().containsKey("loadup.internal"));
    }

    @Test
    void reservedAttributesAreRejectedBeforeCreateOrUpdateWrites() {
        for (String key : Set.of("settings.secret", "loadup.oauth.enabled")) {
            var request =
                    authorization("Billing", Set.of("payments:read"), "payments-api", Map.of(key, "attacker-value"));
            assertThrows(
                    CommonException.class,
                    () -> management.create(
                            REGISTRATION_ID, new OAuthClientCreateRequest(CLIENT_ID, request, EXPIRES_AT)));
            verify(manager, never()).create(any(RegisteredClient.class), any(Instant.class));

            when(manager.get(REGISTRATION_ID)).thenReturn(registeredClient());
            assertThrows(CommonException.class, () -> management.updateAuthorization(REGISTRATION_ID, request));
            verify(manager, never()).updateAuthorization(anyString(), any(RegisteredClient.class));
        }
    }

    @Test
    void invalidPathIdentifiersAreRejectedBeforeManagerAccess() {
        assertThrows(CommonException.class, () -> management.create("invalid/id", createRequest()));
        assertThrows(CommonException.class, () -> management.detail("../billing"));
        assertThrows(CommonException.class, () -> management.detail(".."));
        assertThrows(CommonException.class, () -> management.detail("."));
        assertThrows(CommonException.class, () -> management.detail(null));
        assertThrows(
                CommonException.class,
                () -> management.updateAuthorization(
                        "", authorization("Billing", Set.of("payments:read"), "payments-api", Map.of())));
        verifyNoInteractions(manager);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " billing", "bill\ting"})
    void invalidClientIdsDoNotReachTheManager(String clientId) {
        assertInvalid(() -> management.create(
                REGISTRATION_ID,
                new OAuthClientCreateRequest(clientId, createRequest().authorization(), EXPIRES_AT)));
        verifyNoInteractions(manager);
    }

    @Test
    void rejectsMissingCreateRequestAndOversizedClientId() {
        assertInvalid(() -> management.create(REGISTRATION_ID, null));
        assertInvalid(() -> management.create(
                REGISTRATION_ID,
                new OAuthClientCreateRequest("c".repeat(101), createRequest().authorization(), EXPIRES_AT)));
        verifyNoInteractions(manager);
    }

    @ParameterizedTest
    @MethodSource("invalidAuthorizations")
    void invalidAuthorizationDoesNotCreateAClient(OAuthClientAuthorization authorization) {
        assertInvalid(() ->
                management.create(REGISTRATION_ID, new OAuthClientCreateRequest(CLIENT_ID, authorization, EXPIRES_AT)));
        verifyNoInteractions(manager);
    }

    private static Stream<OAuthClientAuthorization> invalidAuthorizations() {
        var scopes = Set.of("payments:read");
        var audience = "payments-api";
        Map<String, String> attributes = Map.of();
        var tooManyScopes =
                IntStream.rangeClosed(1, 101).mapToObj(i -> "scope-" + i).collect(Collectors.toSet());
        var tooManyAttributes =
                IntStream.rangeClosed(1, 9).boxed().collect(Collectors.toMap(i -> "tag-" + i, i -> "billing"));
        return Stream.of(
                null,
                authorization(null, scopes, audience, attributes),
                authorization(" ", scopes, audience, attributes),
                authorization("n".repeat(201), scopes, audience, attributes),
                authorization(" Billing", scopes, audience, attributes),
                authorization("Bill\ting", scopes, audience, attributes),
                authorization("Billing", null, audience, attributes),
                authorization("Billing", Set.of(), audience, attributes),
                authorization("Billing", tooManyScopes, audience, attributes),
                authorization("Billing", Collections.singleton(null), audience, attributes),
                authorization("Billing", Set.of(" "), audience, attributes),
                authorization("Billing", Set.of("s".repeat(129)), audience, attributes),
                authorization("Billing", scopes, audience, null),
                authorization("Billing", scopes, null, attributes),
                authorization("Billing", scopes, " ", attributes),
                authorization("Billing", scopes, "a".repeat(129), attributes),
                new OAuthClientAuthorization("Billing", scopes, audience, attributes, null),
                new OAuthClientAuthorization("Billing", scopes, audience, attributes, Duration.ZERO),
                new OAuthClientAuthorization("Billing", scopes, audience, attributes, Duration.ofSeconds(-1)),
                authorization("Billing", scopes, audience, tooManyAttributes),
                authorization("Billing", scopes, audience, Collections.singletonMap(null, "billing")),
                authorization("Billing", scopes, audience, Map.of("owner/team", "billing")),
                authorization("Billing", scopes, audience, Collections.singletonMap("owner", null)),
                authorization("Billing", scopes, audience, Map.of("owner", " ")),
                authorization("Billing", scopes, audience, Map.of("owner", "o".repeat(257))),
                authorization("Billing", scopes, audience, Map.of("owner", "bill\ting")));
    }

    @Test
    void lifecycleCommandsPassTheRegistrationAndExpiryToTheManager() {
        var issued = mock(IssuedClientCredential.class);
        when(issued.registrationId()).thenReturn(REGISTRATION_ID);
        when(issued.clientId()).thenReturn(CLIENT_ID);
        when(issued.secret()).thenReturn(SECRET);
        when(issued.expiresAt()).thenReturn(EXPIRES_AT);
        when(manager.rotateCredential(REGISTRATION_ID, EXPIRES_AT)).thenReturn(issued);
        var expiry = new OAuthClientExpiryRequest(EXPIRES_AT);

        assertInvalid(() -> management.setCredentialExpiry(REGISTRATION_ID, null));
        assertInvalid(() -> management.rotateCredential(REGISTRATION_ID, null));
        verifyNoInteractions(manager);

        management.disable(REGISTRATION_ID);
        management.setCredentialExpiry(REGISTRATION_ID, expiry);
        var rotated = management.rotateCredential(REGISTRATION_ID, expiry);

        verify(manager).disable(REGISTRATION_ID);
        verify(manager).setCredentialExpiry(REGISTRATION_ID, EXPIRES_AT);
        assertEquals(new OAuthClientCredentialResponse(REGISTRATION_ID, CLIENT_ID, SECRET, EXPIRES_AT), rotated);
    }

    @Test
    void detailTreatsLegacyAndExplicitlyEnabledClientsAsEnabled() {
        var legacy = registeredClient();
        var enabled = RegisteredClient.from(legacy)
                .clientSettings(
                        ClientSettings.withSettings(legacy.getClientSettings().getSettings())
                                .setting(OAuthClientSettings.ENABLED, true)
                                .build())
                .build();
        when(manager.get(REGISTRATION_ID)).thenReturn(legacy, enabled);
        assertTrue(management.detail(REGISTRATION_ID).enabled());
        assertTrue(management.detail(REGISTRATION_ID).enabled());
    }

    private static void assertInvalid(Executable action) {
        assertEquals(PARAM_ILLEGAL, assertThrows(CommonException.class, action).getResultCode());
    }

    private static OAuthClientCreateRequest createRequest() {
        return new OAuthClientCreateRequest(
                CLIENT_ID, authorization("Billing", Set.of("payments:read"), "payments-api", Map.of()), EXPIRES_AT);
    }

    private static OAuthClientAuthorization authorization(
            String clientName, Set<String> scopes, String audience, Map<String, String> attributes) {
        return new OAuthClientAuthorization(clientName, scopes, audience, attributes, Duration.ofMinutes(10));
    }

    private static RegisteredClient registeredClient() {
        return RegisteredClient.withId(REGISTRATION_ID)
                .clientId(CLIENT_ID)
                .clientIdIssuedAt(ISSUED_AT)
                .clientSecret(STORED_HASH)
                .clientName("Billing integration")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("payments:read")
                .clientSettings(ClientSettings.builder()
                        .setting(OAuthClientSettings.AUDIENCE, "payments-api")
                        .setting("application.owner", "billing")
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(10))
                        .build())
                .build();
    }

    private static RegisteredClient registeredClientWithReservedSettings() {
        return RegisteredClient.from(registeredClient())
                .clientSettings(ClientSettings.builder()
                        .setting(OAuthClientSettings.AUDIENCE, "payments-api")
                        .setting(OAuthClientSettings.ENABLED, false)
                        .setting("application.owner", "billing")
                        .setting("settings.secret", "hidden-settings-value")
                        .setting("loadup.internal", "hidden-loadup-value")
                        .build())
                .build();
    }
}
