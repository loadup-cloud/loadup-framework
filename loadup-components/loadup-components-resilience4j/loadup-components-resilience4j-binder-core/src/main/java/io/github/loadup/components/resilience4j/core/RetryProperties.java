package io.github.loadup.components.resilience4j.core;

import io.github.resilience4j.spring6.retry.configure.RetryConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Standard {@code resilience4j.retry.*} configuration properties.
 */
@ConfigurationProperties(prefix = "resilience4j.retry")
public class RetryProperties extends RetryConfigurationProperties {}
