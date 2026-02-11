package io.github.loadup.components.dfs.s3.autoconfig;

import io.github.loadup.components.dfs.binding.DefaultDfsBinding;
import io.github.loadup.components.dfs.cfg.DfsBindingCfg;
import io.github.loadup.components.dfs.s3.binder.S3DfsBinder;
import io.github.loadup.components.dfs.s3.cfg.S3DfsBinderCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(S3DfsBinder.class)
public class S3DfsAutoConfiguration {

    /**
     * S3 模块的自动注册逻辑 只有当 classpath 下存在 S3DfsBinding 类时（即引入了 binder-s3 依赖），这段逻辑才生效
     */
    @Bean
    public BindingMetadata<?, ?, ?, ?> s3Metadata() {
        return new BindingMetadata<>(
                "s3",
                DefaultDfsBinding.class,
                S3DfsBinder.class,
                DfsBindingCfg.class,
                S3DfsBinderCfg.class,
                ctx -> new DefaultDfsBinding());
    }
}
