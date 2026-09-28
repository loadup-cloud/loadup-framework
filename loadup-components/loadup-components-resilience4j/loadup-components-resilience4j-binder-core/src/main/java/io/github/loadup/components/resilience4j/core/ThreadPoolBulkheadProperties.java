package io.github.loadup.components.resilience4j.core;

import io.github.resilience4j.common.bulkhead.configuration.CommonThreadPoolBulkheadConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Standard {@code resilience4j.thread-pool-bulkhead.*} configuration properties.
 */
@ConfigurationProperties(prefix = "resilience4j.thread-pool-bulkhead")
public class ThreadPoolBulkheadProperties extends CommonThreadPoolBulkheadConfigurationProperties {}
