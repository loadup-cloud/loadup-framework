package io.github.loadup.components.resilience4j.core;

import io.github.resilience4j.spring6.timelimiter.configure.TimeLimiterConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Standard {@code resilience4j.timelimiter.*} configuration properties.
 */
@ConfigurationProperties(prefix = "resilience4j.timelimiter")
public class TimeLimiterProperties extends TimeLimiterConfigurationProperties {}
