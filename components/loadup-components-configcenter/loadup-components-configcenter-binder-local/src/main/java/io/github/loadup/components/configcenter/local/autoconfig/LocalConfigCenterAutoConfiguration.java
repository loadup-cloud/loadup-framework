package io.github.loadup.components.configcenter.local.autoconfig;

import io.github.loadup.components.configcenter.binding.impl.DefaultConfigCenterBinding;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.components.configcenter.local.binder.LocalConfigCenterBinder;
import io.github.loadup.components.configcenter.local.cfg.LocalConfigCenterBinderCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Local binder 自动配置。
 *
 * <p>向 {@link io.github.loadup.components.configcenter.manager.ConfigCenterBindingManager}
 * 注册 Local binder 元数据。
 */
@AutoConfiguration
public class LocalConfigCenterAutoConfiguration {

    @Bean
    public BindingMetadata<?, ?, ?, ?> localConfigCenterMetadata() {
        return new BindingMetadata<>(
                "local",
                DefaultConfigCenterBinding.class,
                LocalConfigCenterBinder.class,
                ConfigCenterBindingCfg.class,
                LocalConfigCenterBinderCfg.class,
                ctx -> new DefaultConfigCenterBinding());
    }
}
