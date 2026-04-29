package io.github.loadup.components.configcenter.apollo.autoconfig;

import com.ctrip.framework.apollo.ConfigService;
import io.github.loadup.components.configcenter.apollo.binder.ApolloConfigCenterBinder;
import io.github.loadup.components.configcenter.apollo.cfg.ApolloConfigCenterBinderCfg;
import io.github.loadup.components.configcenter.binding.impl.DefaultConfigCenterBinding;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Apollo binder 自动配置。
 */
@AutoConfiguration
@ConditionalOnClass(ConfigService.class)
public class ApolloConfigCenterAutoConfiguration {

    @Bean
    public BindingMetadata<?, ?, ?, ?> apolloConfigCenterMetadata() {
        return new BindingMetadata<>(
                "apollo",
                DefaultConfigCenterBinding.class,
                ApolloConfigCenterBinder.class,
                ConfigCenterBindingCfg.class,
                ApolloConfigCenterBinderCfg.class,
                ctx -> new DefaultConfigCenterBinding());
    }
}
