package io.github.loadup.components.configcenter.nacos.autoconfig;

import com.alibaba.nacos.api.NacosFactory;
import io.github.loadup.components.configcenter.binding.impl.DefaultConfigCenterBinding;
import io.github.loadup.components.configcenter.cfg.ConfigCenterBindingCfg;
import io.github.loadup.components.configcenter.nacos.binder.NacosConfigCenterBinder;
import io.github.loadup.components.configcenter.nacos.cfg.NacosConfigCenterBinderCfg;
import io.github.loadup.framework.api.manager.BindingMetadata;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * Nacos binder 自动配置。
 */
@AutoConfiguration
@ConditionalOnClass(NacosFactory.class)
public class NacosConfigCenterAutoConfiguration {

    @Bean
    public BindingMetadata<?, ?, ?, ?> nacosConfigCenterMetadata() {
        return new BindingMetadata<>(
                "nacos",
                DefaultConfigCenterBinding.class,
                NacosConfigCenterBinder.class,
                ConfigCenterBindingCfg.class,
                NacosConfigCenterBinderCfg.class,
                ctx -> new DefaultConfigCenterBinding());
    }
}
