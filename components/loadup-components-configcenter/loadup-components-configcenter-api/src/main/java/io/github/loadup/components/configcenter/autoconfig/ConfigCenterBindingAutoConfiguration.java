package io.github.loadup.components.configcenter.autoconfig;

import io.github.loadup.components.configcenter.manager.ConfigCenterBindingManager;
import io.github.loadup.components.configcenter.properties.ConfigCenterGroupProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * 配置中心核心自动配置。
 *
 * <p>注册 {@link ConfigCenterBindingManager} 单例 Bean，
 * binder 模块（local / nacos / apollo）各自通过 {@link io.github.loadup.framework.api.manager.BindingMetadata}
 * Bean 向管理器注册自己的元数据。
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(ConfigCenterGroupProperties.class)
public class ConfigCenterBindingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConfigCenterBindingManager configCenterBindingManager(
            ConfigCenterGroupProperties props, ApplicationContext context) {
        return new ConfigCenterBindingManager(props, context);
    }
}
