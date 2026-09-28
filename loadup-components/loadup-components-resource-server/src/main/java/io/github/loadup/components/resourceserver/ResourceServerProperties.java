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

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration for JWT validation and the optional default API security chain. */
@ConfigurationProperties(prefix = "loadup.security.resource-server")
public class ResourceServerProperties {
    private boolean enabled;
    private boolean defaultSecurityFilterChain = true;
    private String issuerUri;
    private String jwkSetUri;
    private String audience;
    private String accessTokenUseClaim = "token_use";
    private String accessTokenUseValue = "access";
    private List<String> permitAll = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDefaultSecurityFilterChain() {
        return defaultSecurityFilterChain;
    }

    public void setDefaultSecurityFilterChain(boolean defaultSecurityFilterChain) {
        this.defaultSecurityFilterChain = defaultSecurityFilterChain;
    }

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getAccessTokenUseClaim() {
        return accessTokenUseClaim;
    }

    public void setAccessTokenUseClaim(String accessTokenUseClaim) {
        this.accessTokenUseClaim = accessTokenUseClaim;
    }

    public String getAccessTokenUseValue() {
        return accessTokenUseValue;
    }

    public void setAccessTokenUseValue(String accessTokenUseValue) {
        this.accessTokenUseValue = accessTokenUseValue;
    }

    public List<String> getPermitAll() {
        return permitAll;
    }

    public void setPermitAll(List<String> permitAll) {
        this.permitAll = permitAll;
    }
}
