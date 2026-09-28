package io.github.loadup.components.globalunique;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Global unique component configuration. */
@ConfigurationProperties(prefix = "loadup.global-unique")
public class GlobalUniqueProperties {
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
