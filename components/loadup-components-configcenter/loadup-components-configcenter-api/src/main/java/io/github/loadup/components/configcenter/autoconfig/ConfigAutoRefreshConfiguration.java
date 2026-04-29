package io.github.loadup.components.configcenter.autoconfig;

import io.github.loadup.components.configcenter.model.ConfigChangeEvent;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

/**
 * 可选的配置自动刷新配置。
 *
 * <p>当 classpath 中存在 {@code spring-cloud-context} 时才激活，
 * 监听 {@link ConfigChangeEvent} 并触发 Spring Cloud 的 {@link ContextRefresher#refresh()}，
 * 从而自动刷新 {@code @Value} / {@code @ConfigurationProperties} 等绑定的 Bean。
 *
 * <p>通过 {@link io.github.loadup.components.configcenter.annotation.EnableConfigAutoRefresh}
 * 注解按需导入本配置类，不自动激活。
 */
@AutoConfiguration
@ConditionalOnClass(ContextRefresher.class)
public class ConfigAutoRefreshConfiguration {

    @Bean
    public ApplicationListener<ConfigChangeEvent> configAutoRefreshListener(ContextRefresher contextRefresher) {
        return event -> {
            contextRefresher.refresh();
        };
    }
}
