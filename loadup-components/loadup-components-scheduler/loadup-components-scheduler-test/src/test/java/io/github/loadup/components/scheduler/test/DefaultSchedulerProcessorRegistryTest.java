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
