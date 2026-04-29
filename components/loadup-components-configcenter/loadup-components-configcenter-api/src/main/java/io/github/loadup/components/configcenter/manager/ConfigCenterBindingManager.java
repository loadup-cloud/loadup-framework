package io.github.loadup.components.configcenter.manager;

import io.github.loadup.components.configcenter.binder.ConfigCenterBinder;
import io.github.loadup.components.configcenter.binding.ConfigCenterBinding;
import io.github.loadup.components.configcenter.properties.ConfigCenterGroupProperties;
import io.github.loadup.framework.api.manager.BindingManagerSupport;
import org.springframework.context.ApplicationContext;

/**
 * Binding manager for the config center.
 *
 * <p>Manages all registered binder metadata via the Spring container and
 * lazily creates {@link ConfigCenterBinding} instances by binding name at runtime.
 *
 * <pre>
 * // Use the default binding
 * ConfigCenterBinding binding = manager.getBinding();
 *
 * // Use a named binding (matching loadup.configcenter.bindings.xxx)
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
