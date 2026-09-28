package io.github.loadup.components.configcenter.local;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loadup.configcenter.binder.local")
public class LocalConfigCenterProperties {
    // Local binder has no extra options; kept as a placeholder for symmetric configuration binding.
}
