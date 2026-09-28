package io.github.loadup.components.resourceserver;

/*-
 * #%L
 * LoadUp Cloud
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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class ResourceServerTokenValidatorTest {
    @Test
    void acceptsOnlyAccessTokensForExpectedIssuerAndAudience() {
        ResourceServerProperties properties = new ResourceServerProperties();
        properties.setIssuerUri("https://issuer.example");
        properties.setAudience("loadup-api");
        var validator = ResourceServerAutoConfiguration.accessTokenValidator(properties);
        assertThat(validator
                        .validate(token("access", "loadup-api", "https://issuer.example"))
                        .hasErrors())
                .isFalse();
        assertThat(validator
                        .validate(token("refresh", "loadup-api", "https://issuer.example"))
                        .hasErrors())
                .isTrue();
        assertThat(validator
                        .validate(token("access", "other-api", "https://issuer.example"))
                        .hasErrors())
                .isTrue();
        assertThat(validator
                        .validate(token("access", "loadup-api", "https://other.example"))
                        .hasErrors())
                .isTrue();
        properties.setAccessTokenUseClaim("");
        assertThat(ResourceServerAutoConfiguration.accessTokenValidator(properties)
                        .validate(token("external", "loadup-api", "https://issuer.example"))
                        .hasErrors())
                .isFalse();
    }

    private static Jwt token(String use, String audience, String issuer) {
        return Jwt.withTokenValue("test")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .issuer(issuer)
                .audience(List.of(audience))
                .claim("token_use", use)
                .build();
    }
}
