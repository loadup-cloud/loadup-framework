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
package io.github.loadup.components.serviceauth;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("loadup.security.service-auth")
public class ServiceAuthProperties {
    private String tokenUri;
    private String clientSecret;
    private boolean allowInsecureHttp;
    private Duration connectTimeout = Duration.ofSeconds(2);
    private Duration readTimeout = Duration.ofSeconds(5);
    private Map<String, Registration> registrations = new LinkedHashMap<>();

    public String getTokenUri() {
        return tokenUri;
    }

    public void setTokenUri(String value) {
        tokenUri = value;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String value) {
        clientSecret = value;
    }

    public boolean isAllowInsecureHttp() {
        return allowInsecureHttp;
    }

    public void setAllowInsecureHttp(boolean value) {
        allowInsecureHttp = value;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration value) {
        connectTimeout = value;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration value) {
        readTimeout = value;
    }

    public Map<String, Registration> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(Map<String, Registration> value) {
        registrations = value;
    }

    public record Registration(
            String clientId, String tenantId, String serviceId, String audience, List<String> scopes) {
        public Registration {
            if (scopes == null
                    || scopes.isEmpty()
                    || scopes.stream()
                            .anyMatch(
                                    scope -> scope == null || !scope.matches("[\\x21\\x23-\\x5B\\x5D-\\x7E]{1,124}"))) {
                throw new IllegalArgumentException("Service registrations require explicit business scopes");
            }
            scopes = List.copyOf(scopes);
        }
    }

    @Override
    public String toString() {
        return "ServiceAuthProperties[credentials hidden]";
    }
}
