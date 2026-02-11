package com.github.loadup.components.scheduler.xxljob.autoconfig;

import com.github.loadup.components.scheduler.xxljob.cfg.XxlJobSchedulerBinderCfg;
import io.github.loadup.components.scheduler.binding.DefaultSchedulerBinding;
import io.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import io.github.loadup.components.scheduler.xxljob.binder.XxlJobSchedulerBinder;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(XxlJobSchedulerBinder.class)
public class XxlJobSchedulerAutoConfiguration {


    @Bean
    public BindingMetadata<?, ?, ?, ?> simpleJobMetadata() {
        return new BindingMetadata<>(
            "xxljob",
            DefaultSchedulerBinding.class,
            XxlJobSchedulerBinder.class,
            SchedulerBindingCfg.class,
            XxlJobSchedulerBinderCfg.class,
            ctx -> new DefaultSchedulerBinding());
    }
}
