package io.github.loadup.components.resilience4j.core;

import io.github.resilience4j.spring6.bulkhead.configure.BulkheadConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Standard {@code resilience4j.bulkhead.*} configuration properties.
 */
@ConfigurationProperties(prefix = "resilience4j.bulkhead")
public class BulkheadProperties extends BulkheadConfigurationProperties {}
