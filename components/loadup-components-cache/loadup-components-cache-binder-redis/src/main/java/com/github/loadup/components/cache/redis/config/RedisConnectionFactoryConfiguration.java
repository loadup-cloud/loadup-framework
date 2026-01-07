package com.github.loadup.components.cache.redis.config;

/*-
 * #%L
 * loadup-components-cache-binder-redis
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

import com.github.loadup.components.cache.redis.cfg.RedisBinderCfg;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.StringUtils;

/**
 * Redis Connection Factory Configuration
 *
 * <p>This configuration supports two modes:
 *
 * <ol>
 *   <li>Default mode: Use Spring Boot's auto-configured RedisConnectionFactory
 *   <li>Override mode: Create custom RedisConnectionFactory with loadup.cache.redis.* configuration
 * </ol>
 *
 * <p>The custom factory is only created when loadup.cache.redis has custom configuration.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({RedisBinderCfg.class, RedisProperties.class})
@ConditionalOnProperty(prefix = "loadup.cache", name = "binder", havingValue = "redis")
public class RedisConnectionFactoryConfiguration {

  @Autowired(required = false)
  private RedisProperties springRedisProperties;

  @Autowired private RedisBinderCfg redisBinderCfg;

  /**
   * Create custom RedisConnectionFactory if loadup.cache.binder.redis has custom configuration
   * Otherwise, Spring Boot's auto-configured factory will be used
   */
  @Bean
  @Conditional(RedisBinderCondition.class)
  public RedisConnectionFactory customRedisConnectionFactory() {
    log.info("Creating custom RedisConnectionFactory with loadup.cache.binder.redis configuration");

    // Merge configurations (custom overrides default)
    RedisConfiguration redisConfig = buildRedisConfiguration();

    // Build Lettuce client configuration
    LettuceClientConfiguration clientConfig = buildLettuceClientConfiguration();

    // Create connection factory
    LettuceConnectionFactory factory =
        new LettuceConnectionFactory((RedisStandaloneConfiguration) redisConfig, clientConfig);

    factory.afterPropertiesSet();

    log.info("Custom RedisConnectionFactory created successfully");
    return factory;
  }

  /** Build Redis configuration based on deployment pattern */
  private RedisConfiguration buildRedisConfiguration() {
    String pattern = determinePattern();

    return switch (pattern.toLowerCase()) {
      case "sentinel" -> buildSentinelConfiguration();
      case "cluster" -> buildClusterConfiguration();
      default -> buildStandaloneConfiguration();
    };
  }

  /** Determine Redis deployment pattern */
  private String determinePattern() {
    // Priority 1: Custom configuration
    if (StringUtils.hasText(redisBinderCfg.getPattern())) {
      return redisBinderCfg.getPattern();
    }

    // Priority 2: Auto-detect from custom nodes configuration
    if (redisBinderCfg.getSentinelNodes() != null && redisBinderCfg.getSentinelNodes().length > 0) {
      return "sentinel";
    }
    if (redisBinderCfg.getClusterNodes() != null && redisBinderCfg.getClusterNodes().length > 0) {
      return "cluster";
    }

    // Priority 3: Auto-detect from Spring configuration
    if (springRedisProperties != null) {
      if (springRedisProperties.getSentinel() != null) {
        return "sentinel";
      }
      if (springRedisProperties.getCluster() != null) {
        return "cluster";
      }
    }

    return "standalone";
  }

  /** Build standalone configuration */
  private RedisStandaloneConfiguration buildStandaloneConfiguration() {
    RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();

    // Host
    String host =
        getConfigValue(
            redisBinderCfg.getHost(),
            springRedisProperties != null ? springRedisProperties.getHost() : null,
            "localhost");
    config.setHostName(host);

    // Port
    Integer port =
        getConfigValue(
            redisBinderCfg.getPort(),
            springRedisProperties != null ? springRedisProperties.getPort() : null,
            6379);
    config.setPort(port);

    // Database
    Integer database =
        getConfigValue(
            redisBinderCfg.getDatabase(),
            springRedisProperties != null ? springRedisProperties.getDatabase() : null,
            0);
    config.setDatabase(database);

    // Username (Redis 6+ ACL)
    String username =
        getConfigValue(
            redisBinderCfg.getUsername(),
            springRedisProperties != null ? springRedisProperties.getUsername() : null,
            null);
    if (StringUtils.hasText(username)) {
      config.setUsername(username);
    }

    // Password
    String password =
        getConfigValue(
            redisBinderCfg.getPassword(),
            springRedisProperties != null ? springRedisProperties.getPassword() : null,
            null);
    if (StringUtils.hasText(password)) {
      config.setPassword(RedisPassword.of(password));
    }

    log.info("Redis standalone configuration: host={}, port={}, database={}", host, port, database);
    return config;
  }

  /** Build sentinel configuration */
  private RedisSentinelConfiguration buildSentinelConfiguration() {
    RedisSentinelConfiguration config = new RedisSentinelConfiguration();

    // Master name
    String master =
        getConfigValue(
            redisBinderCfg.getSentinelMaster(),
            springRedisProperties != null && springRedisProperties.getSentinel() != null
                ? springRedisProperties.getSentinel().getMaster()
                : null,
            "mymaster");
    config.setMaster(master);

    // Sentinel nodes
    String[] nodes =
        getConfigValue(
            redisBinderCfg.getSentinelNodes(),
            springRedisProperties != null && springRedisProperties.getSentinel() != null
                ? springRedisProperties.getSentinel().getNodes().toArray(new String[0])
                : null,
            new String[0]);

    for (String node : nodes) {
      String[] parts = node.split(":");
      if (parts.length == 2) {
        config.sentinel(parts[0], Integer.parseInt(parts[1]));
      }
    }

    // Database
    Integer database =
        getConfigValue(
            redisBinderCfg.getDatabase(),
            springRedisProperties != null ? springRedisProperties.getDatabase() : null,
            0);
    config.setDatabase(database);

    // Password
    String password =
        getConfigValue(
            redisBinderCfg.getPassword(),
            springRedisProperties != null ? springRedisProperties.getPassword() : null,
            null);
    if (StringUtils.hasText(password)) {
      config.setPassword(RedisPassword.of(password));
    }

    // Sentinel password
    String sentinelPassword =
        getConfigValue(
            redisBinderCfg.getSentinelPassword(),
            springRedisProperties != null && springRedisProperties.getSentinel() != null
                ? springRedisProperties.getSentinel().getPassword()
                : null,
            null);
    if (StringUtils.hasText(sentinelPassword)) {
      config.setSentinelPassword(RedisPassword.of(sentinelPassword));
    }

    log.info("Redis sentinel configuration: master={}, nodes={}", master, nodes.length);
    return config;
  }

  /** Build cluster configuration */
  private RedisClusterConfiguration buildClusterConfiguration() {
    RedisClusterConfiguration config = new RedisClusterConfiguration();

    // Cluster nodes
    String[] nodes =
        getConfigValue(
            redisBinderCfg.getClusterNodes(),
            springRedisProperties != null && springRedisProperties.getCluster() != null
                ? springRedisProperties.getCluster().getNodes().toArray(new String[0])
                : null,
            new String[0]);

    List<RedisNode> redisNodes = new ArrayList<>();
    for (String node : nodes) {
      String[] parts = node.split(":");
      if (parts.length == 2) {
        redisNodes.add(new RedisNode(parts[0], Integer.parseInt(parts[1])));
      }
    }
    config.setClusterNodes(redisNodes);

    // Max redirects
    Integer maxRedirects =
        getConfigValue(
            redisBinderCfg.getMaxRedirects(),
            springRedisProperties != null && springRedisProperties.getCluster() != null
                ? springRedisProperties.getCluster().getMaxRedirects()
                : null,
            3);
    config.setMaxRedirects(maxRedirects);

    // Password
    String password =
        getConfigValue(
            redisBinderCfg.getPassword(),
            springRedisProperties != null ? springRedisProperties.getPassword() : null,
            null);
    if (StringUtils.hasText(password)) {
      config.setPassword(RedisPassword.of(password));
    }

    log.info("Redis cluster configuration: nodes={}, maxRedirects={}", nodes.length, maxRedirects);
    return config;
  }

  /** Build Lettuce client configuration */
  private LettuceClientConfiguration buildLettuceClientConfiguration() {
    LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder =
        LettucePoolingClientConfiguration.builder();

    // Connection timeout
    Long connectTimeout =
        getConfigValue(
            redisBinderCfg.getConnectTimeout(),
            springRedisProperties != null && springRedisProperties.getConnectTimeout() != null
                ? springRedisProperties.getConnectTimeout().toMillis()
                : null,
            2000L);

    // Read timeout
    Long readTimeout =
        getConfigValue(
            redisBinderCfg.getReadTimeout(),
            springRedisProperties != null && springRedisProperties.getTimeout() != null
                ? springRedisProperties.getTimeout().toMillis()
                : null,
            2000L);

    // Socket options
    SocketOptions socketOptions =
        SocketOptions.builder().connectTimeout(Duration.ofMillis(connectTimeout)).build();

    ClientOptions clientOptions = ClientOptions.builder().socketOptions(socketOptions).build();

    builder.clientOptions(clientOptions);
    builder.commandTimeout(Duration.ofMillis(readTimeout));

    // Connection pool
    GenericObjectPoolConfig<?> poolConfig = buildPoolConfig();
    builder.poolConfig(poolConfig);

    // SSL
    Boolean sslEnabled =
        getConfigValue(
            redisBinderCfg.getSslEnabled(),
            springRedisProperties != null
                    && springRedisProperties.getSsl() != null
                    && springRedisProperties.getSsl().isEnabled()
                ? true
                : null,
            false);
    if (sslEnabled) {
      builder.useSsl();
    }

    return builder.build();
  }

  /** Build connection pool configuration */
  private GenericObjectPoolConfig<?> buildPoolConfig() {
    GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();

    // Max active connections
    Integer maxActive =
        getConfigValue(
            redisBinderCfg.getMaxActive(),
            springRedisProperties != null
                    && springRedisProperties.getLettuce() != null
                    && springRedisProperties.getLettuce().getPool() != null
                ? springRedisProperties.getLettuce().getPool().getMaxActive()
                : null,
            8);
    poolConfig.setMaxTotal(maxActive);

    // Max idle connections
    Integer maxIdle =
        getConfigValue(
            redisBinderCfg.getMaxIdle(),
            springRedisProperties != null
                    && springRedisProperties.getLettuce() != null
                    && springRedisProperties.getLettuce().getPool() != null
                ? springRedisProperties.getLettuce().getPool().getMaxIdle()
                : null,
            8);
    poolConfig.setMaxIdle(maxIdle);

    // Min idle connections
    Integer minIdle =
        getConfigValue(
            redisBinderCfg.getMinIdle(),
            springRedisProperties != null
                    && springRedisProperties.getLettuce() != null
                    && springRedisProperties.getLettuce().getPool() != null
                ? springRedisProperties.getLettuce().getPool().getMinIdle()
                : null,
            0);
    poolConfig.setMinIdle(minIdle);

    // Max wait time
    Long maxWait =
        getConfigValue(
            redisBinderCfg.getMaxWait(),
            springRedisProperties != null
                    && springRedisProperties.getLettuce() != null
                    && springRedisProperties.getLettuce().getPool() != null
                    && springRedisProperties.getLettuce().getPool().getMaxWait() != null
                ? springRedisProperties.getLettuce().getPool().getMaxWait().toMillis()
                : null,
            -1L);
    poolConfig.setMaxWait(Duration.ofMillis(maxWait));

    return poolConfig;
  }

  /** Get configuration value with priority: custom > default > fallback */
  private <T> T getConfigValue(T customValue, T defaultValue, T fallbackValue) {
    if (customValue != null) {
      return customValue;
    }
    if (defaultValue != null) {
      return defaultValue;
    }
    return fallbackValue;
  }

  public static class RedisBinderCondition extends AnyNestedCondition {

    public RedisBinderCondition() {
      super(ConfigurationPhase.REGISTER_BEAN);
    }

    // 检查 host 属性
    @ConditionalOnProperty(prefix = "loadup.cache.binder.redis", name = "host")
    static class HostProperty {}

    // 检查 database 属性
    @ConditionalOnProperty(prefix = "loadup.cache.binder.redis", name = "database")
    static class DatabaseProperty {}

    // 如果还有其他属性，可以在这里继续添加
    @ConditionalOnProperty(prefix = "loadup.cache.binder.redis", name = "nodes")
    static class NodesProperty {}
  }
}
