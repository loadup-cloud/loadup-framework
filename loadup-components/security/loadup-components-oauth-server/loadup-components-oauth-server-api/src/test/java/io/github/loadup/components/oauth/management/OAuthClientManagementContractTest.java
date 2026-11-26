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

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OAuthClientManagementContractTest {
    private static final Instant EXPIRES_AT = Instant.parse("2030-01-02T03:04:05Z");
    private static final OAuthClientAuthorization AUTHORIZATION = new OAuthClientAuthorization(
            "Payment worker",
            Set.of("payments:read"),
            "payments-api",
            Map.of("app", "payments"),
            Duration.ofMinutes(5));

    @Test
    void validatesClientCreationAndNestedAuthorization() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            assertThat(validator.validate(new OAuthClientCreateRequest("payment-worker", AUTHORIZATION, EXPIRES_AT)))
                    .isEmpty();

            var invalid = new OAuthClientAuthorization(" ", Set.of(), "", null, null);
            assertThat(validator.validate(new OAuthClientCreateRequest("", invalid, null)))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactlyInAnyOrder(
                            "clientId",
                            "authorization.clientName",
                            "authorization.scopes",
                            "authorization.audience",
                            "authorization.attributes",
                            "authorization.accessTokenTtl",
                            "expiresAt");
        }
    }

    @Test
    void requiresAnExpiryWhenUpdatingCredentialExpiry() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var request = new OAuthClientExpiryRequest(EXPIRES_AT);
            assertThat(validator.validate(request)).isEmpty();
            assertThat(validator.validate(new OAuthClientExpiryRequest(null)))
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("expiresAt");
            assertThat(request.toString()).contains("\"expiresAt\":\"2030-01-02T03:04:05Z\"");
        }
    }

    @Test
    void diagnosticsRedactCredentialsAndAuthorization() {
        var request = new OAuthClientCreateRequest("payment-worker", AUTHORIZATION, EXPIRES_AT);
        var details = new OAuthClientDetails("registration-1", "payment-worker", AUTHORIZATION, true, EXPIRES_AT);
        var credential = new OAuthClientCredentialResponse(
                "registration-1", "payment-worker", "synthetic-client-secret", EXPIRES_AT);

        assertThat(AUTHORIZATION.toString()).contains("\"audience\":\"payments-api\"");
        assertThat(request.toString())
                .contains("\"clientId\":\"payment-worker\"", "\"authorization\":\"******\"")
                .doesNotContain("payments:read");
        assertThat(details.toString())
                .contains("\"enabled\":true", "\"authorization\":\"******\"")
                .doesNotContain("payments:read");
        assertThat(credential.secret()).isEqualTo("synthetic-client-secret");
        assertThat(credential.toString())
                .contains("\"registrationId\":\"registration-1\"", "\"secret\":\"******\"")
                .doesNotContain(credential.secret());
    }
}
