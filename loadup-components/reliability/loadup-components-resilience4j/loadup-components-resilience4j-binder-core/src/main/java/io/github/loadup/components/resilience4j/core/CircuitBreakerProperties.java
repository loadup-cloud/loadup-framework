package io.github.loadup.components.resilience4j.core;

import io.github.resilience4j.spring6.circuitbreaker.configure.CircuitBreakerConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Standard {@code resilience4j.circuitbreaker.*} configuration properties.
 */
@ConfigurationProperties(prefix = "resilience4j.circuitbreaker")
public class CircuitBreakerProperties extends CircuitBreakerConfigurationProperties {}
