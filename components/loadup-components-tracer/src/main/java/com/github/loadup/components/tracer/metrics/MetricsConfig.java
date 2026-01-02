package com.github.loadup.components.tracer.metrics;

/*-
 * #%L
 * loadup-components-tracer
 * %%
 * Copyright (C) 2022 - 2025 loadup_cloud
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

import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.concurrent.Executor;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Metrics configuration for integrating various infrastructure components with Micrometer.
 * Provides metrics for HikariCP connection pools, Redis (Lettuce), and thread pools.
 */
@Slf4j
@Configuration
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(
    prefix = "loadup.tracer",
    name = "metrics-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class MetricsConfig {

  /**
   * Configure Prometheus registry for metrics export.
   *
   * @return PrometheusMeterRegistry configured for Prometheus endpoint
   */
  @Bean
  @ConditionalOnClass(name = "io.micrometer.prometheus.PrometheusMeterRegistry")
  public PrometheusMeterRegistry prometheusMeterRegistry() {
    log.info("Configuring Prometheus metrics registry");
    return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
  }

  /**
   * Customize MeterRegistry with common tags.
   *
   * @param applicationName Spring application name
   * @return MeterRegistryCustomizer with application-level tags
   */
  @Bean
  public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(
      @org.springframework.beans.factory.annotation.Value("${spring.application.name:loadup-app}")
          String applicationName) {
    return registry ->
        registry.config().commonTags("application", applicationName, "framework", "loadup");
  }

  /**
   * Register HikariCP metrics when HikariDataSource is available.
   *
   * @param dataSource The HikariCP DataSource
   * @param meterRegistry Micrometer MeterRegistry
   */
  @Bean
  @ConditionalOnBean(DataSource.class)
  @ConditionalOnClass(HikariDataSource.class)
  public HikariMetricsRegistrar hikariMetricsRegistrar(
      @Autowired(required = false) DataSource dataSource, MeterRegistry meterRegistry) {
    if (dataSource instanceof HikariDataSource hikariDataSource) {
      log.info("Registering HikariCP metrics");
      hikariDataSource.setMetricRegistry(meterRegistry);
    }
    return new HikariMetricsRegistrar();
  }

  /**
   * Register Lettuce (Redis) metrics when available.
   *
   * @param lettuceConnectionFactory The Lettuce connection factory
   * @param meterRegistry Micrometer MeterRegistry
   */
  @Bean
  @ConditionalOnBean(LettuceConnectionFactory.class)
  @ConditionalOnClass(name = "org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory")
  public LettuceMetricsRegistrar lettuceMetricsRegistrar(
      @Autowired(required = false) LettuceConnectionFactory lettuceConnectionFactory,
      MeterRegistry meterRegistry) {
    if (lettuceConnectionFactory != null) {
      log.info("Registering Lettuce (Redis) metrics");
      // Lettuce metrics are automatically enabled via Micrometer when LettuceConnectionFactory is
      // present
      // Additional custom metrics can be added here if needed
    }
    return new LettuceMetricsRegistrar();
  }

  /**
   * Register thread pool metrics for custom executors.
   *
   * @param meterRegistry Micrometer MeterRegistry
   */
  @Bean
  @ConditionalOnBean(Executor.class)
  public ThreadPoolMetricsRegistrar threadPoolMetricsRegistrar(
      MeterRegistry meterRegistry, @Autowired(required = false) Executor executor) {
    if (executor instanceof java.util.concurrent.ExecutorService executorService) {
      log.info("Registering thread pool metrics");
      ExecutorServiceMetrics.monitor(meterRegistry, executorService, "application-executor");
    } else if (executor != null) {
      log.warn("Executor is not an ExecutorService, skipping metrics registration");
    }
    return new ThreadPoolMetricsRegistrar();
  }

  /** Marker class for HikariCP metrics registration */
  static class HikariMetricsRegistrar {}

  /** Marker class for Lettuce metrics registration */
  static class LettuceMetricsRegistrar {}

  /** Marker class for thread pool metrics registration */
  static class ThreadPoolMetricsRegistrar {}
}
