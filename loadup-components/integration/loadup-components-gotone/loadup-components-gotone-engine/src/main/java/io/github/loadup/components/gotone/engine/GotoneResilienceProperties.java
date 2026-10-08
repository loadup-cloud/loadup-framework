package io.github.loadup.components.gotone.engine;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Gotone resilience configuration ({@code loadup.gotone.resilience.*}).
 *
 * <p>When enabled and a resilience4j binder is present, every channel provider is wrapped
 * with a per-provider circuit breaker and retry. Instance names follow the
 * {@code gotone-<channelType>-<providerName>} convention and are configured with the standard
 * {@code resilience4j.*} properties.
 */
@ConfigurationProperties(prefix = "loadup.gotone.resilience")
public class GotoneResilienceProperties {

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
