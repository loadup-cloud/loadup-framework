package io.github.loadup.components.extension.config;

import io.github.loadup.components.extension.annotation.BizScenario;
import io.github.loadup.components.extension.exector.ExtensionExecutor;
import io.github.loadup.components.extension.interceptor.BizScenarioInterceptor;
import io.github.loadup.components.extension.register.ExtensionRegistry;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/** Extension 框架的 Spring Boot 自动装配类 */
@AutoConfiguration
@ConditionalOnClass({ExtensionExecutor.class, ExtensionRegistry.class})
public class ExtensionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExtensionRegistry extensionRegistry() {
        return new ExtensionRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public ExtensionExecutor extensionExecutor(ExtensionRegistry extensionRegistry) {
        return new ExtensionExecutor(extensionRegistry);
    }

    /**
     * 注册 {@link BizScenario @BizScenario} AOP 拦截器。
     *
     * <p>拦截所有标注了 {@code @BizScenario} 的 Service 方法，在方法执行前后自动管理
     * {@link io.github.loadup.components.extension.context.BizContextHolder} 的生命周期。
     */
    @Bean
    @ConditionalOnMissingBean(BizScenarioInterceptor.class)
    public BizScenarioInterceptor bizScenarioInterceptor() {
        return new BizScenarioInterceptor();
    }

    @Bean
    public Advisor bizScenarioAdvisor(BizScenarioInterceptor bizScenarioInterceptor) {
        AnnotationMatchingPointcut pointcut = new AnnotationMatchingPointcut(null, BizScenario.class, true);
        return new DefaultPointcutAdvisor(pointcut, bizScenarioInterceptor);
    }
}
