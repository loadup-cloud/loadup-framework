package com.github.loadup.components.extension.config;

import com.github.loadup.components.extension.exector.ExtensionExecutor;
import com.github.loadup.components.extension.register.ExtensionRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Extension 框架的 Spring Boot 自动装配类
 */
@AutoConfiguration
@ConditionalOnClass({ExtensionExecutor.class, ExtensionRegistry.class}) // 条件：只有当核心类存在时，此配置才生效
public class ExtensionAutoConfiguration {

    /**
     * 自动配置 ExtensionRegistry Bean
     * @ConditionalOnMissingBean: 允许用户覆盖默认的 Bean。如果用户自己定义了一个 ExtensionRegistry，则此配置不生效。
     */
    @Bean
    @ConditionalOnMissingBean
    public ExtensionRegistry extensionRegistry() {
        return new ExtensionRegistry();
    }

    /**
     * 自动配置 ExtensionExecutor Bean
     * @ConditionalOnMissingBean: 同样允许用户自定义覆盖。
     * @param extensionRegistry Spring会自动从上下文中注入上面创建的 extensionRegistry Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public ExtensionExecutor extensionExecutor(ExtensionRegistry extensionRegistry) {
        return new ExtensionExecutor(extensionRegistry);
    }
}