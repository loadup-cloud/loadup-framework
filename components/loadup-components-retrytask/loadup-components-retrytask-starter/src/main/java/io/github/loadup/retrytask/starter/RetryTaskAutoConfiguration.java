package io.github.loadup.retrytask.starter;

/*-
 * #%L
 * Loadup Components Retrytask Starter
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

import io.github.loadup.components.gotone.api.NotificationService;
import io.github.loadup.retrytask.core.*;
import io.github.loadup.retrytask.core.config.RetryTaskProperties;
import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.infra.repository.JdbcRetryTaskRepository;
import io.github.loadup.retrytask.infra.repository.RetryTaskRepository;
import io.github.loadup.retrytask.notify.GotoneRetryTaskNotifier;
import io.github.loadup.retrytask.notify.LoggingRetryTaskNotifier;
import io.github.loadup.retrytask.notify.RetryTaskNotifier;
import io.github.loadup.retrytask.notify.RetryTaskNotifierRegistry;
import io.github.loadup.retrytask.strategy.*;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Auto-configuration for the retry task module
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RetryTaskProperties.class)
public class RetryTaskAutoConfiguration {

    // ========== Strategy Beans ==========

    @Bean
    @ConditionalOnMissingBean(name = "fixedRetryStrategy")
    public RetryStrategy fixedRetryStrategy() {
        log.info(">>> [RETRY-TASK] Initializing FixedRetryStrategy");
        return new FixedRetryStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(name = "exponentialBackoffRetryStrategy")
    public RetryStrategy exponentialBackoffRetryStrategy() {
        log.info(">>> [RETRY-TASK] Initializing ExponentialBackoffRetryStrategy");
        return new ExponentialBackoffRetryStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(name = "randomWaitRetryStrategy")
    public RetryStrategy randomWaitRetryStrategy() {
        log.info(">>> [RETRY-TASK] Initializing RandomWaitRetryStrategy");
        return new RandomWaitRetryStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(name = "incrementalWaitRetryStrategy")
    public RetryStrategy incrementalWaitRetryStrategy() {
        log.info(">>> [RETRY-TASK] Initializing IncrementalWaitRetryStrategy");
        return new IncrementalWaitRetryStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(name = "fibonacciBackoffRetryStrategy")
    public RetryStrategy fibonacciBackoffRetryStrategy() {
        log.info(">>> [RETRY-TASK] Initializing FibonacciBackoffRetryStrategy");
        return new FibonacciBackoffRetryStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryStrategyRegistry retryStrategyRegistry(List<RetryStrategy> strategies) {
        log.info(">>> [RETRY-TASK] Initializing RetryStrategyRegistry with {} strategies", strategies.size());
        return new RetryStrategyRegistry(strategies);
    }

    // ========== Notifier Beans ==========

    @Bean
    @ConditionalOnMissingBean(name = "loggingRetryTaskNotifier")
    public RetryTaskNotifier loggingRetryTaskNotifier() {
        log.info(">>> [RETRY-TASK] Initializing LoggingRetryTaskNotifier");
        return new LoggingRetryTaskNotifier();
    }

    @Bean
    @ConditionalOnMissingBean(name = "gotoneRetryTaskNotifier")
    @ConditionalOnProperty(prefix = "loadup.retrytask.notifier.gotone", name = "enabled", havingValue = "true")
    public RetryTaskNotifier gotoneRetryTaskNotifier(NotificationService notificationService) {
        log.info(">>> [RETRY-TASK] Initializing GotoneRetryTaskNotifier");
        return new GotoneRetryTaskNotifier(notificationService);
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTaskNotifierRegistry retryTaskNotifierRegistry(List<RetryTaskNotifier> notifiers) {
        log.info(">>> [RETRY-TASK] Initializing RetryTaskNotifierRegistry with {} notifiers", notifiers.size());
        return new RetryTaskNotifierRegistry(notifiers);
    }

    // ========== Core Beans ==========

    @Bean
    @ConditionalOnMissingBean
    public RetryTaskProcessorRegistry retryTaskProcessorRegistry(List<RetryTaskProcessor> processors) {
        log.info(">>> [RETRY-TASK] Initializing RetryTaskProcessorRegistry with {} processors", processors.size());
        return new RetryTaskProcessorRegistry(processors);
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTaskRepository retryTaskRepository(JdbcTemplate jdbcTemplate, RetryTaskProperties properties) {
        log.info(
                ">>> [RETRY-TASK] Initializing JdbcRetryTaskRepository with table prefix: {}, dbType: {}",
                properties.getTablePrefix(),
                properties.getDbType());
        return new JdbcRetryTaskRepository(jdbcTemplate, properties.getTablePrefix(), properties.getDbType());
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTaskService retryTaskService(
            RetryTaskRepository retryTaskRepository,
            RetryTaskNotifierRegistry retryTaskNotifierRegistry,
            RetryStrategyRegistry retryStrategyRegistry,
            RetryTaskProperties retryTaskProperties,
            @Lazy RetryTaskExecutor retryTaskExecutor) {
        log.info(">>> [RETRY-TASK] Initializing RetryTaskService");
        return new RetryTaskServiceImpl(
                retryTaskRepository,
                retryTaskNotifierRegistry,
                retryStrategyRegistry,
                retryTaskProperties,
                retryTaskExecutor);
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTaskExecutor retryTaskExecutor(
            RetryTaskProcessorRegistry processorRegistry, RetryTaskService retryTaskService) {
        log.info(">>> [RETRY-TASK] Initializing RetryTaskExecutor");
        return new RetryTaskExecutor(processorRegistry, retryTaskService, null);
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryTaskFacade retryTaskFacade(RetryTaskService retryTaskService) {
        log.info(">>> [RETRY-TASK] Initializing RetryTaskFacade (alias for Service)");
        // Since RetryTaskServiceImpl implements RetryTaskFacade, we can cast it.
        return (RetryTaskFacade) retryTaskService;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
            prefix = "loadup.retrytask",
            name = "scheduler.enabled",
            havingValue = "true",
            matchIfMissing = true)
    public RetryTaskScheduler retryTaskScheduler(
            RetryTaskService retryTaskService,
            RetryTaskExecutor retryTaskExecutor,
            RetryTaskProperties retryTaskProperties) {
        return new RetryTaskScheduler(retryTaskService, retryTaskExecutor, retryTaskProperties);
    }
}
