package io.github.loadup.common.tracer;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Test configuration for tracer component tests.
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = "io.github.loadup.common.tracer")
public class TestConfiguration {}
