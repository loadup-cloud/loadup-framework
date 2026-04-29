package io.github.loadup.components.configcenter.manager;

import io.github.loadup.components.configcenter.binder.ConfigCenterBinder;
import io.github.loadup.components.configcenter.binding.ConfigCenterBinding;
import io.github.loadup.components.configcenter.properties.ConfigCenterGroupProperties;
import io.github.loadup.framework.api.manager.BindingManagerSupport;
import org.springframework.context.ApplicationContext;

/**
 * 配置中心绑定管理器。
 *
 * <p>通过 Spring 容器管理所有已注册的 binder 元数据，
 * 运行时按 binding name 懒加载 {@link ConfigCenterBinding} 实例。
 *
 * <pre>
 * // 使用默认 binding
 * ConfigCenterBinding binding = manager.getBinding();
 *
 * // 使用命名 binding（对应 loadup.configcenter.bindings.xxx）
 * ConfigCenterBinding nacos = manager.getBinding("my-nacos-config");
 * </pre>
 */
public class ConfigCenterBindingManager extends BindingManagerSupport<ConfigCenterBinder, ConfigCenterBinding> {

    private final ConfigCenterGroupProperties groupProps;

    public ConfigCenterBindingManager(ConfigCenterGroupProperties props, ApplicationContext context) {
        super(context, "loadup.configcenter");
        this.groupProps = props;
    }

    @Override
    protected String getDefaultBinderType() {
        return groupProps.getDefaultBinder().getValue();
    }

    @Override
    public Class<ConfigCenterBinding> getBindingInterface() {
        return ConfigCenterBinding.class;
    }

    @Override
    public Class<ConfigCenterBinder> getBinderInterface() {
        return ConfigCenterBinder.class;
    }
}
