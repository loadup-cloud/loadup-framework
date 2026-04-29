package io.github.loadup.components.configcenter.cfg;

import io.github.loadup.framework.api.cfg.BaseBinderCfg;
import lombok.Getter;
import lombok.Setter;

/**
 * Base configuration for all config-center binders.
 *
 * <p>All concrete binder configs (Local / Nacos / Apollo) must extend this class.
 */
@Getter
@Setter
public abstract class ConfigCenterBinderCfg extends BaseBinderCfg {

    /** Namespace (Nacos namespace / Apollo appId prefix, etc.). */
    private String namespace = "";

    /** Default group used when no group is explicitly specified. */
    private String defaultGroup = "DEFAULT_GROUP";

    @Override
    public Object getIdentity() {
        return namespace + ":" + defaultGroup;
    }
}
