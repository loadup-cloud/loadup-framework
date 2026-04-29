package io.github.loadup.components.configcenter.local.cfg;

import io.github.loadup.components.configcenter.cfg.ConfigCenterBinderCfg;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration for the local-file binder.
 *
 * <pre>
 * loadup:
 *   configcenter:
 *     binders:
 *       local:
 *         base-path: config/        # root directory for local config files (relative or absolute)
 *         refresh-interval: 30s     # polling interval for file-change detection
 * </pre>
 */
@Getter
@Setter
public class LocalConfigCenterBinderCfg extends ConfigCenterBinderCfg {

    /**
     * Root directory for local config files.
     * File path rule: {basePath}/{group}/{dataId}
     * Falls back to Spring {@code Environment.getProperty(dataId)} when the file does not exist.
     */
    private String basePath = "config";

    /**
     * Polling interval for detecting file changes
     * (used to trigger {@link io.github.loadup.components.configcenter.model.ConfigChangeListener}).
     */
    private Duration refreshInterval = Duration.ofSeconds(30);

    @Override
    public Object getIdentity() {
        return basePath;
    }
}
