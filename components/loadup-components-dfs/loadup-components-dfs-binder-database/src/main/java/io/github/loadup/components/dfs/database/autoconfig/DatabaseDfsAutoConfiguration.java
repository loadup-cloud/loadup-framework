package io.github.loadup.components.dfs.database.autoconfig;

import io.github.loadup.components.dfs.binding.DefaultDfsBinding;
import io.github.loadup.components.dfs.cfg.DfsBindingCfg;
import io.github.loadup.components.dfs.database.binder.DatabaseDfsBinder;
import io.github.loadup.components.dfs.database.cfg.DatabaseDfsBinderCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(DatabaseDfsBinder.class)
public class DatabaseDfsAutoConfiguration {
    /**
     * Database 模块的自动注册逻辑
     */
    @Bean
    public BindingMetadata<?, ?, ?, ?> dbMetadata() {
        return new BindingMetadata<>(
                "database",
                DefaultDfsBinding.class,
                DatabaseDfsBinder.class,
                DfsBindingCfg.class,
                DatabaseDfsBinderCfg.class,
                ctx -> new DefaultDfsBinding());
    }
}
