package io.github.loadup.components.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/** Counts API envelope outcomes independently of the HTTP status code. */
public final class ApiResultMetrics {
    private final Counter successes;
    private final Counter failures;

    public ApiResultMetrics(MeterRegistry registry) {
        this.successes = registry.counter("loadup.api.responses", "outcome", "success");
        this.failures = registry.counter("loadup.api.responses", "outcome", "failure");
    }

    public void record(boolean success) {
        (success ? successes : failures).increment();
    }
}
