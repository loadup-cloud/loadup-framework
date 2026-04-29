package io.github.loadup.components.configcenter.cfg;

import io.github.loadup.framework.api.cfg.BaseBindingCfg;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置中心 Binding 业务配置。
 *
 * <p>每个 binding（命名空间/业务标识）可以指定自己的 dataId / group，
 * 覆盖 binder 层的默认值。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConfigCenterBindingCfg extends BaseBindingCfg {

    /** 配置项 ID（Key），不指定时使用 bizTag 作为 dataId。 */
    private String dataId;

    /** 配置分组，不指定时使用 binder 层 defaultGroup。 */
    private String group;
}
