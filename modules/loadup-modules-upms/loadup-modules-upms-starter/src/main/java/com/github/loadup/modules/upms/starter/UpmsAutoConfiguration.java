package com.github.loadup.modules.upms.starter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * UPMS Auto Configuration
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@AutoConfiguration
@EnableAsync
@EnableConfigurationProperties
// @EnableJdbcRepositories(basePackages =
// "com.github.loadup.modules.upms.infrastructure.repository")
@MapperScan("com.github.loadup.modules.upms.infrastructure.mapper")
@ComponentScan(
    basePackages = {
      "com.github.loadup.modules.upms.domain",
      "com.github.loadup.modules.upms.infrastructure",
      "com.github.loadup.modules.upms.app",
      "com.github.loadup.modules.upms.security",
      "com.github.loadup.modules.upms.adapter"
    })
public class UpmsAutoConfiguration {
  // Auto-configuration for UPMS module
}
