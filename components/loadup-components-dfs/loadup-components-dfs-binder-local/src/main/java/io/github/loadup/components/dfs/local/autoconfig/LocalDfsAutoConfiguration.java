package io.github.loadup.components.dfs.local.autoconfig;

import io.github.loadup.components.dfs.binding.DefaultDfsBinding;
import io.github.loadup.components.dfs.cfg.DfsBindingCfg;
import io.github.loadup.components.dfs.local.binder.LocalDfsBinder;
import io.github.loadup.components.dfs.local.cfg.LocalDfsBinderCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(LocalDfsBinder.class)
public class LocalDfsAutoConfiguration {
    /**
     * Local 模块的自动注册逻辑
     */
    @Bean
    public BindingMetadata<?, ?, ?, ?> dbMetadata() {
        return new BindingMetadata<>(
            "local",
            DefaultDfsBinding.class,
            LocalDfsBinder.class,
            DfsBindingCfg.class,
            LocalDfsBinderCfg.class,
            ctx -> new DefaultDfsBinding());
    }
}
