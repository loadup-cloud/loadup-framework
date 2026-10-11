/*
 * #%L
 * Loadup Components Scheduler Test
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
package io.github.loadup.components.scheduler.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.components.scheduler.DefaultSchedulerProcessorRegistry;
import io.github.loadup.components.scheduler.SchedulerProcessor;
import io.github.loadup.components.scheduler.SchedulerProcessorRegistry;
import io.github.loadup.components.scheduler.model.SchedulerContext;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultSchedulerProcessorRegistryTest {

    private static SchedulerProcessor processor(String taskName) {
        return new SchedulerProcessor() {
            @Override
            public String taskName() {
                return taskName;
            }

            @Override
            public void process(SchedulerContext context) {}
        };
    }

    @Test
    void resolvesProcessorByTaskName() {
        SchedulerProcessorRegistry registry =
                new DefaultSchedulerProcessorRegistry(List.of(processor("a"), processor("b")));

        assertThat(registry.getProcessor("b").taskName()).isEqualTo("b");
    }

    @Test
    void unknownTaskNameIsRejected() {
        SchedulerProcessorRegistry registry = new DefaultSchedulerProcessorRegistry(List.of(processor("a")));

        assertThatThrownBy(() -> registry.getProcessor("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void duplicateTaskNameIsRejected() {
        assertThatThrownBy(() -> new DefaultSchedulerProcessorRegistry(List.of(processor("a"), processor("a"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");
    }
}
