package io.github.loadup.components.configcenter.cfg;

import io.github.loadup.framework.api.cfg.BaseBinderCfg;
import lombok.Getter;
import lombok.Setter;

/**
 * 配置中心 Binder 基础配置。
 *
 * <p>所有具体 binder（Local / Nacos / Apollo）的配置类必须继承此类。
 */
@Getter
@Setter
public abstract class ConfigCenterBinderCfg extends BaseBinderCfg {

    /** 命名空间（Nacos namespace / Apollo appId prefix 等）。 */
    private String namespace = "";

    /** 默认分组，未指定时使用。 */
    private String defaultGroup = "DEFAULT_GROUP";

    @Override
    public Object getIdentity() {
        return namespace + ":" + defaultGroup;
    }
}
