package com.github.loadup.components.scheduler.powerjob.autoconfig;

import com.github.loadup.components.scheduler.powerjob.cfg.PowerJobSchedulerBinderCfg;
import io.github.loadup.components.scheduler.binding.DefaultSchedulerBinding;
import io.github.loadup.components.scheduler.binding.SchedulerBinding;
import io.github.loadup.components.scheduler.powerjob.binder.PowerJobSchedulerBinder;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

@AutoConfiguration
@ConditionalOnClass(BasicProcessor.class)
public class PowerJobSchedulerAutoConfiguration {


    /**
     * S3 模块的自动注册逻辑 只有当 classpath 下存在 S3DfsBinding 类时（即引入了 binder-s3 依赖），这段逻辑才生效
     */
    @Bean
    public BindingMetadata<?, ?, ?, ?> s3Metadata() {
        return new BindingMetadata<>(
            "powerjob",
            DefaultSchedulerBinding.class,
            PowerJobSchedulerBinder.class,
            SchedulerBinding.class,
            PowerJobSchedulerBinderCfg.class,
            ctx -> new DefaultSchedulerBinding());
    }
}
