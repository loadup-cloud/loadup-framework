/*
 * #%L
 * LoadUp Observability
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.github.loadup.components.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Counts API envelope outcomes independently of the HTTP status code.
 */
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
