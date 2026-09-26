package io.github.loadup.gateway.webmvc.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/** Configuration used by the managed SCG MVC gateway. */
@ConfigurationProperties(prefix = "loadup.gateway")
public class GatewayProperties {
    private boolean enabled = true;
    private int routeRefreshInterval = 5;

    @NestedConfigurationProperty
    private Security security = new Security();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getRouteRefreshInterval() {
        return routeRefreshInterval;
    }

    public void setRouteRefreshInterval(int routeRefreshInterval) {
        this.routeRefreshInterval = routeRefreshInterval;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public static class Security {
        private boolean enabled;
        private String secret;
        private String issuerUri;
        private String jwkSetUri;
        private Map<String, String> appSecrets = Map.of();
        private int maxSignedBodyBytes = 1_048_576;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
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

        public Map<String, String> getAppSecrets() {
            return appSecrets;
        }

        public void setAppSecrets(Map<String, String> appSecrets) {
            this.appSecrets = appSecrets;
        }

        public int getMaxSignedBodyBytes() {
            return maxSignedBodyBytes;
        }

        public void setMaxSignedBodyBytes(int maxSignedBodyBytes) {
            this.maxSignedBodyBytes = maxSignedBodyBytes;
        }
    }
}
