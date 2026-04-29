package io.github.loadup.components.configcenter.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Global properties for the config center.
 *
 * <pre>
 * loadup:
 *   configcenter:
 *     default-binder: local    # local | nacos | apollo
 * </pre>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "loadup.configcenter")
public class ConfigCenterGroupProperties {

    /** Default binder type used when none is specified at the binding level. */
    private ConfigCenterBinderType defaultBinder = ConfigCenterBinderType.LOCAL;
}
