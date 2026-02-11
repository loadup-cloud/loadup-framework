package io.github.loadup.components.scheduler.simplejob.autoconfig;

import io.github.loadup.components.scheduler.binding.DefaultSchedulerBinding;
import io.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import io.github.loadup.components.scheduler.simplejob.binder.SimpleJobSchedulerBinder;
import io.github.loadup.components.scheduler.simplejob.cfg.SimpleJobSchedulerBinderCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(SimpleJobSchedulerBinder.class)
public class SimpleJobSchedulerAutoConfiguration {


    @Bean
    public BindingMetadata<?, ?, ?, ?> simpleJobMetadata() {
        return new BindingMetadata<>(
            "simplejob",
            DefaultSchedulerBinding.class,
            SimpleJobSchedulerBinder.class,
            SchedulerBindingCfg.class,
            SimpleJobSchedulerBinderCfg.class,
            ctx -> new DefaultSchedulerBinding());
    }
}
