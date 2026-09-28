package io.github.loadup.components.resilience4j.core;

import io.github.resilience4j.spring6.ratelimiter.configure.RateLimiterConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Standard {@code resilience4j.ratelimiter.*} configuration properties.
 */
@ConfigurationProperties(prefix = "resilience4j.ratelimiter")
public class RateLimiterProperties extends RateLimiterConfigurationProperties {}
