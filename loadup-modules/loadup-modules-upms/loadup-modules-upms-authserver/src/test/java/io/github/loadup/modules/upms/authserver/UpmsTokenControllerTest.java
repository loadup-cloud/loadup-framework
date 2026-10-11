/*
 * #%L
 * LoadUp UPMS Authorization Server Adapter
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
package io.github.loadup.modules.upms.authserver;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.loadup.components.authserver.properties.LoadUpAuthServerProperties;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.*;

class UpmsTokenControllerTest {
    @Test
    void issuesTokensUsingInjectedClockAndRedactsDiagnosticCredentials() {
        var provider = mock(UpmsAuthenticationProvider.class);
        var encoder = mock(JwtEncoder.class);
        var properties = new LoadUpAuthServerProperties();
        properties.setAudience("test-api");
        var principal = new UpmsPrincipal("user", "example", List.of());
        when(provider.authenticate(any(), anyString()))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of()));
        Instant now = Instant.parse("2026-10-11T00:00:00Z");
        when(encoder.encode(any())).thenAnswer(invocation -> {
            JwtEncoderParameters parameters = invocation.getArgument(0);
            assertThat(parameters.getClaims().getIssuedAt()).isEqualTo(now);
            assertThat(parameters.getClaims().getExpiresAt()).isEqualTo(now.plus(properties.getUserAccessTokenTtl()));
            return Jwt.withTokenValue("signed-token")
                    .header("alg", "RS256")
                    .claims(claims -> claims.putAll(parameters.getClaims().getClaims()))
                    .build();
        });
        var controller =
                new UpmsTokenController(provider, encoder, properties, Clock.fixed(now, ZoneId.of("Asia/Shanghai")));
        var request = new UpmsTokenController.LoginRequest("example", "private-password");
        controller.login(request, new MockHttpServletRequest());
        verify(encoder).encode(any());
        assertThat(request.toString()).doesNotContain("private-password");
        assertThat(new UpmsTokenController.TokenData("private-token", "Bearer", 30).toString())
                .doesNotContain("private-token");
    }
}
