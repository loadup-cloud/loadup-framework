package io.github.loadup.components.configcenter.cfg;

import io.github.loadup.framework.api.cfg.BaseBindingCfg;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Business-level binding configuration for the config center.
 *
 * <p>Each binding (namespace/bizTag) may specify its own dataId / group
 * to override the binder-level defaults.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigCenterBindingCfg extends BaseBindingCfg {

    /** Config item ID (key). When unset, the bizTag is used as the dataId. */
    private String dataId;

    /** Config group. When unset, falls back to the binder-level defaultGroup. */
    private String group;
}
