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

import static org.assertj.core.api.Assertions.*;

import io.github.loadup.commons.context.ContextHolder;
import io.github.loadup.commons.context.ContextKeys;
import io.github.loadup.commons.context.ExecutionContext;
import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.context.ThreadLocalAccessor;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.CompositeTaskDecorator;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;

class LoadUpContextPropagationTest {
    @Test
    void composesScopedBusinessBindingsWithMicrometerAndRestoresBoth() throws Exception {
        var marker = new ThreadLocal<String>();
        var registry = new ContextRegistry().registerThreadLocalAccessor(new ThreadLocalAccessor<String>() {
            public Object key() {
                return "test.marker";
            }

            public String getValue() {
                return marker.get();
            }

            public void setValue(String value) {
                marker.set(value);
            }

            public void setValue() {
                marker.remove();
            }
        });
        var snapshots = ContextSnapshotFactory.builder()
                .contextRegistry(registry)
                .clearMissing(true)
                .build();
        var decorator = new CompositeTaskDecorator(
                List.of(new LoadUpContextTaskDecorator(), new ContextPropagatingTaskDecorator(snapshots)));
        marker.set("caller-marker");
        try {
            Runnable task = ContextHolder.callWith(
                    ExecutionContext.empty().with(ContextKeys.TENANT_ID, "caller"),
                    () -> decorator.decorate(() -> {
                        assertThat(ContextHolder.get(ContextKeys.TENANT_ID)).isEqualTo("caller");
                        assertThat(marker.get()).isEqualTo("caller-marker");
                        throw new IllegalStateException("failed");
                    }));
            marker.set("worker-marker");
            ContextHolder.runWith(ExecutionContext.empty().with(ContextKeys.TENANT_ID, "worker"), () -> {
                assertThatThrownBy(task::run).isInstanceOf(IllegalStateException.class);
                assertThat(ContextHolder.get(ContextKeys.TENANT_ID)).isEqualTo("worker");
                assertThat(marker.get()).isEqualTo("worker-marker");
            });
            Runnable empty =
                    decorator.decorate(() -> assertThat(ContextHolder.isEmpty()).isTrue());
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                executor.submit(() -> ContextHolder.runWith(
                                ExecutionContext.empty().with(ContextKeys.TENANT_ID, "worker"), empty))
                        .get();
            }
        } finally {
            marker.remove();
        }
        assertThat(ContextHolder.isBound()).isFalse();
    }

    @Test
    void bootExecutorUsesBusinessDecoratorAlongsideStandardPropagation() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        ObservabilityAutoConfiguration.class, TaskExecutionAutoConfiguration.class))
                .withBean(io.micrometer.core.instrument.MeterRegistry.class, SimpleMeterRegistry::new)
                .withPropertyValues(
                        "spring.task.execution.propagate-context=true", "spring.threads.virtual.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(LoadUpContextTaskDecorator.class);
                    var executor = context.getBean("applicationTaskExecutor", AsyncTaskExecutor.class);
                    ContextHolder.callWith(ExecutionContext.empty().with(ContextKeys.TENANT_ID, "tenant"), () -> {
                        var value = executor.submit(() -> ContextHolder.get(ContextKeys.TENANT_ID));
                        assertThat(value.get()).isEqualTo("tenant");
                        return null;
                    });
                });
    }
}
