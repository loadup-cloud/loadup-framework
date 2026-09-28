package io.github.loadup.retrytask.test.jobrunr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.loadup.retrytask.facade.RetryTaskProcessor;
import io.github.loadup.retrytask.facade.RetryTaskProcessorRegistry;
import io.github.loadup.retrytask.facade.model.RetryTaskContext;
import io.github.loadup.retrytask.jobrunr.DefaultRetryTaskProcessorRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultRetryTaskProcessorRegistryTest {

    private static RetryTaskProcessor processor(String bizType) {
        return new RetryTaskProcessor() {
            @Override
            public String bizType() {
                return bizType;
            }

            @Override
            public void process(RetryTaskContext context) {}
        };
    }

    @Test
    void resolvesProcessorByBizType() {
        RetryTaskProcessorRegistry registry =
                new DefaultRetryTaskProcessorRegistry(List.of(processor("a"), processor("b")));

        assertThat(registry.getProcessor("b").bizType()).isEqualTo("b");
    }

    @Test
    void unknownBizTypeIsRejected() {
        RetryTaskProcessorRegistry registry = new DefaultRetryTaskProcessorRegistry(List.of(processor("a")));

        assertThatThrownBy(() -> registry.getProcessor("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void duplicateBizTypeIsRejected() {
        assertThatThrownBy(() -> new DefaultRetryTaskProcessorRegistry(List.of(processor("a"), processor("a"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate");
    }
}
