package io.github.loadup.components.resilience4j.core;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.loadup.components.resilience4j.ResilienceRegistries;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class Resilience4jCoreAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(Resilience4jCoreAutoConfiguration.class));

    @Test
    void assemblesRegistriesByDefault() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ResilienceRegistries.class);
            assertThat(context).hasSingleBean(CircuitBreakerRegistry.class);
            assertThat(context).hasSingleBean(RetryRegistry.class);
            assertThat(context.getBean(ResilienceRegistries.class).circuitBreakerRegistry())
                    .isSameAs(context.getBean(CircuitBreakerRegistry.class));
        });
    }

    @Test
    void disabledViaProperty() {
        contextRunner.withPropertyValues("loadup.resilience4j.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(ResilienceRegistries.class);
        });
    }

    @Test
    void createsConfiguredCircuitBreakerInstance() {
        contextRunner
                .withPropertyValues(
                        "resilience4j.circuitbreaker.instances.demo.failure-rate-threshold=25",
                        "resilience4j.circuitbreaker.instances.demo.sliding-window-size=4")
                .run(context -> {
                    CircuitBreakerRegistry registry = context.getBean(CircuitBreakerRegistry.class);
                    assertThat(registry.getAllCircuitBreakers())
                            .anyMatch(circuitBreaker -> circuitBreaker.getName().equals("demo"));
                });
    }
}
