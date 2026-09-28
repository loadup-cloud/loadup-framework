package io.github.loadup.components.resilience4j;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Top-level LoadUp resilience4j configuration ({@code loadup.resilience4j.*}).
 *
 * <p>{@code enabled} switches the whole component; {@code binder-type} selects the backend.
 * Only the in-memory {@code core} binder exists today; a Redis binder is the planned extension
 * point for distributed circuit breaker / rate limiter state.
 */
@ConfigurationProperties(prefix = "loadup.resilience4j")
public class Resilience4jProperties {

    private boolean enabled = true;

    private String binderType = "core";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBinderType() {
        return binderType;
    }

    public void setBinderType(String binderType) {
        this.binderType = binderType;
    }
}
