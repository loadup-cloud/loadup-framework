/*-
 * #%L
 * Loadup Components Retrytask Test
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
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
