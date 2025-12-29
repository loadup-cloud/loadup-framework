package com.github.loadup.components.tracer;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Test configuration for tracer component tests.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.github.loadup.components.tracer")
public class TestConfiguration {
}

