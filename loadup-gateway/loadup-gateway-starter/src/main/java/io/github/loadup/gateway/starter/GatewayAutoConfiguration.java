package io.github.loadup.gateway.starter;

/*-
 * #%L
 * LoadUp Gateway Starter
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.github.loadup.gateway.core.action.*;
import io.github.loadup.gateway.core.handler.GatewayHandlerAdapter;
import io.github.loadup.gateway.core.handler.GatewayHandlerMapping;
import io.github.loadup.gateway.core.plugin.PluginManager;
import io.github.loadup.gateway.core.router.RouteResolver;
import io.github.loadup.gateway.core.security.SecurityStrategyManager;
import io.github.loadup.gateway.core.template.TemplateEngine;
import io.github.loadup.gateway.facade.config.GatewayProperties;
import io.github.loadup.gateway.facade.spi.ProxyProcessor;
import io.github.loadup.gateway.facade.spi.RepositoryPlugin;
import io.github.loadup.gateway.facade.spi.SecurityStrategy;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Gateway auto-configuration.
 *
 * <p>All beans are explicitly defined here without component scanning.
 * This includes:
 * <ul>
 *   <li>Core Actions (ExceptionAction, RouteAction, SecurityAction, etc.)</li>
 *   <li>Optional TracingAction (when tracer is enabled)</li>
 *   <li>Repository Plugins (FileRepositoryPlugin, DatabaseRepositoryPlugin)</li>
 *   <li>Proxy Processors (HttpProxyProcessor, RpcProxyProcessor, SpringBeanProxyProcessor)</li>
 *   <li>Handler components</li>
 * </ul>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayAutoConfiguration {

    // ========================================
    // Core Components
    // ========================================

    @Bean
    @ConditionalOnMissingBean
    public RouteResolver routeResolver(RepositoryPlugin repositoryPlugin, GatewayProperties gatewayProperties) {
        return new RouteResolver(repositoryPlugin, gatewayProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public PluginManager pluginManager(List<ProxyProcessor> proxyProcessors) {
        return new PluginManager(proxyProcessors);
    }

    @Bean
    @ConditionalOnMissingBean
    public TemplateEngine templateEngine() {
        return new TemplateEngine();
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityStrategyManager securityStrategyManager(
            @Autowired(required = false) List<SecurityStrategy> strategies) {
        return new SecurityStrategyManager(strategies);
    }

    // ========================================
    // Security Strategies
    // ========================================

    /**
     * Default JWT-based security strategy.
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultSecurityStrategy")
    public SecurityStrategy defaultSecurityStrategy(GatewayProperties gatewayProperties) {
        try {
            Class<?> strategyClass = Class.forName("io.github.loadup.gateway.core.security.DefaultSecurityStrategy");
            SecurityStrategy strategy = (SecurityStrategy) strategyClass
                    .getDeclaredConstructor(GatewayProperties.class)
                    .newInstance(gatewayProperties);
            log.info(">>> [GATEWAY] DefaultSecurityStrategy (code: {}) initialized", strategy.getCode());
            return strategy;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DefaultSecurityStrategy", e);
        }
    }

    /**
     * Internal call security strategy.
     */
    @Bean
    @ConditionalOnMissingBean(name = "internalSecurityStrategy")
    public SecurityStrategy internalSecurityStrategy() {
        try {
            Class<?> strategyClass = Class.forName("io.github.loadup.gateway.core.security.InternalSecurityStrategy");
            SecurityStrategy strategy =
                    (SecurityStrategy) strategyClass.getDeclaredConstructor().newInstance();
            log.info(">>> [GATEWAY] InternalSecurityStrategy (code: {}) initialized", strategy.getCode());
            return strategy;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize InternalSecurityStrategy", e);
        }
    }

    /**
     * HMAC-SHA256 signature security strategy.
     */
    @Bean
    @ConditionalOnMissingBean(name = "signatureSecurityStrategy")
    public SecurityStrategy signatureSecurityStrategy() {
        try {
            Class<?> strategyClass = Class.forName("io.github.loadup.gateway.core.security.SignatureSecurityStrategy");
            SecurityStrategy strategy =
                    (SecurityStrategy) strategyClass.getDeclaredConstructor().newInstance();
            log.info(">>> [GATEWAY] SignatureSecurityStrategy (code: {}) initialized", strategy.getCode());
            return strategy;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SignatureSecurityStrategy", e);
        }
    }

    // ========================================
    // Actions
    // ========================================

    @Bean
    @ConditionalOnMissingBean
    public ExceptionAction exceptionAction() {
        return new ExceptionAction();
    }

    @Bean
    @ConditionalOnMissingBean
    public RouteAction routeAction(RouteResolver routeResolver) {
        return new RouteAction(routeResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProxyAction proxyAction(PluginManager pluginManager) {
        return new ProxyAction(pluginManager);
    }

    @Bean
    @ConditionalOnMissingBean
    public RequestTemplateAction requestTemplateAction(TemplateEngine templateEngine) {
        return new RequestTemplateAction(templateEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseTemplateAction responseTemplateAction(TemplateEngine templateEngine) {
        return new ResponseTemplateAction(templateEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResponseWrapperAction responseWrapperAction(GatewayProperties gatewayProperties) {
        return new ResponseWrapperAction(gatewayProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityAction securityAction(SecurityStrategyManager strategyManager) {
        return new SecurityAction(strategyManager);
    }

    /**
     * TracingAction - Optional, only created when tracer is enabled.
     */
    @Bean
    @ConditionalOnClass(Tracer.class)
    @ConditionalOnProperty(prefix = "loadup.tracer", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public TracingAction tracingAction(
            @Autowired(required = false) Tracer tracer, @Autowired(required = false) TextMapPropagator propagator) {
        if (tracer == null || propagator == null) {
            log.warn(">>> [GATEWAY] Tracer or TextMapPropagator not available, skipping TracingAction");
            return null;
        }
        log.info(">>> [GATEWAY] Distributed tracing enabled");
        return new TracingAction(tracer, propagator);
    }

    // ========================================
    // Action Dispatcher
    // ========================================

    @Bean
    @ConditionalOnMissingBean
    public ActionDispatcher actionDispatcher(
            ExceptionAction exceptionAction,
            RouteAction routeAction,
            SecurityAction securityAction,
            RequestTemplateAction requestTemplateAction,
            ResponseWrapperAction responseWrapperAction,
            ResponseTemplateAction responseTemplateAction,
            ProxyAction proxyAction,
            @Autowired(required = false) TracingAction tracingAction) {

        // Build action chain dynamically with optional TracingAction
        List<GatewayAction> actionChain = new ArrayList<>();
        actionChain.add(exceptionAction); // 0. 异常处理

        // Add TracingAction if available (should be early in chain)
        if (tracingAction != null) {
            actionChain.add(tracingAction); // 1. 分布式追踪
            log.info(">>> [GATEWAY] TracingAction added to action chain at position 1");
        }

        actionChain.add(routeAction); // 2. 路由寻址
        actionChain.add(securityAction); // 3. 安全检查
        actionChain.add(requestTemplateAction); // 4. 处理请求参数
        actionChain.add(proxyAction); // 5. 发送请求（最内层）
        actionChain.add(responseTemplateAction); // 6. 转换结果
        actionChain.add(responseWrapperAction); // 7. 包装结果

        log.info(">>> [GATEWAY] ActionDispatcher initialized with {} actions", actionChain.size());
        return new ActionDispatcher(actionChain);
    }

    // ========================================
    // Handlers
    // ========================================

    @Bean
    @ConditionalOnMissingBean
    public GatewayHandlerAdapter gatewayHandlerAdapter(ActionDispatcher actionDispatcher) {
        return new GatewayHandlerAdapter(actionDispatcher);
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayHandlerMapping gatewayHandlerMapping(RepositoryPlugin repositoryPlugin) {
        return new GatewayHandlerMapping(repositoryPlugin);
    }
}
