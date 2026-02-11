package io.github.loadup.components.scheduler.quartz.autoconfig;

import io.github.loadup.components.scheduler.binding.DefaultSchedulerBinding;
import io.github.loadup.components.scheduler.cfg.SchedulerBindingCfg;
import io.github.loadup.components.scheduler.quartz.binder.QuartzJobSchedulerBinder;
import io.github.loadup.components.scheduler.quartz.cfg.QuartzJobSchedulerBinderCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(QuartzJobSchedulerBinder.class)
public class QuartzJobSchedulerAutoConfiguration {

    /**
     *
     */
    @Bean
    public BindingMetadata<?, ?, ?, ?> quartzJobMetadata() {
        return new BindingMetadata<>(
                "quartzjob",
                DefaultSchedulerBinding.class,
                QuartzJobSchedulerBinder.class,
                SchedulerBindingCfg.class,
                QuartzJobSchedulerBinderCfg.class,
                ctx -> new DefaultSchedulerBinding());
    }
}
