/*-
 * #%L
 * Loadup Components Retrytask Test
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
