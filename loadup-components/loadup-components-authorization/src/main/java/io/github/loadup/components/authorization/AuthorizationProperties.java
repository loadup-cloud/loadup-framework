package io.github.loadup.components.authorization;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * LoadUp method security configuration ({@code loadup.security.method-security.*}).
 */
@ConfigurationProperties(prefix = "loadup.security.method-security")
public class AuthorizationProperties {

    /** Master switch for the authorization auto-configuration. */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
