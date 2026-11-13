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
package io.github.loadup.components.authserver.properties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.security.auth-server")
public class LoadUpAuthServerProperties {

    /**
     * Issuer identifier of this authorization server (e.g. {@code http://localhost:8080}).
     */
    private String issuer = "http://localhost:8080";

    private String audience;

    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private boolean protocolEndpointsEnabled = true;

    private Duration userAccessTokenTtl = Duration.ofMinutes(30);
    /**
     * KMS signing reference and retained verification keys used by the SAS binder.
     */
    private Signing signing = new Signing();

    public boolean isProtocolEndpointsEnabled() {
        return protocolEndpointsEnabled;
    }

    public void setProtocolEndpointsEnabled(boolean protocolEndpointsEnabled) {
        this.protocolEndpointsEnabled = protocolEndpointsEnabled;
    }

    public Duration getUserAccessTokenTtl() {
        return userAccessTokenTtl;
    }

    public void setUserAccessTokenTtl(Duration userAccessTokenTtl) {
        this.userAccessTokenTtl = userAccessTokenTtl;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Signing getSigning() {
        return signing;
    }

    public void setSigning(Signing signing) {
        this.signing = signing;
    }

    /**
     * An explicit active key and historical keys to publish through JWKS.
     */
    public static class Signing {
        private SigningKey activeKey = new SigningKey();
        private List<SigningKey> verificationKeys = new ArrayList<>();

        public SigningKey getActiveKey() {
            return activeKey;
        }

        public void setActiveKey(SigningKey activeKey) {
            this.activeKey = activeKey;
        }

        public List<SigningKey> getVerificationKeys() {
            return verificationKeys;
        }

        public void setVerificationKeys(List<SigningKey> verificationKeys) {
            this.verificationKeys = verificationKeys;
        }
    }

    /** A KMS name and pinned version; key IDs are derived as name-vN. */
    public static class SigningKey {
        private String name;
        private int version = 1;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }
    }
}
