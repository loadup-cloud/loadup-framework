package io.github.loadup.modules.upms.starter;

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
// "io.github.loadup.modules.upms.infrastructure.repository")
@MapperScan("io.github.loadup.modules.upms.infrastructure.mapper")
@ComponentScan(
    basePackages = {
      "io.github.loadup.modules.upms.domain",
      "io.github.loadup.modules.upms.infrastructure",
      "io.github.loadup.modules.upms.app",
      "io.github.loadup.modules.upms.security",
      "io.github.loadup.modules.upms.adapter",
      "io.github.loadup.modules.upms.app.service"
    })
public class UpmsAutoConfiguration {
  // Auto-configuration for UPMS module
}
