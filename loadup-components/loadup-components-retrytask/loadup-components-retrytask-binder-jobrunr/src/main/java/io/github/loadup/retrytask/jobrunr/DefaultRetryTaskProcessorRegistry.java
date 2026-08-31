/*-
 * #%L
 * Loadup Components Retrytask Binder JobRunr
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

package io.github.loadup.retrytask.jobrunr;

import io.github.loadup.retrytask.facade.RetryTaskProcessor;
import io.github.loadup.retrytask.facade.RetryTaskProcessorRegistry;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects all {@link RetryTaskProcessor} beans and resolves them by business type.
 */
public class DefaultRetryTaskProcessorRegistry implements RetryTaskProcessorRegistry {

    private final Map<String, RetryTaskProcessor> processors;

    public DefaultRetryTaskProcessorRegistry(List<RetryTaskProcessor> processors) {
        this.processors = processors.stream()
                .collect(Collectors.toUnmodifiableMap(
                        RetryTaskProcessor::bizType, Function.identity(), (first, duplicate) -> {
                            throw new IllegalStateException(
                                    "Duplicate RetryTaskProcessor for bizType '" + duplicate.bizType() + "'");
                        }));
    }

    @Override
    public RetryTaskProcessor getProcessor(String bizType) {
        RetryTaskProcessor processor = processors.get(bizType);
        if (processor == null) {
            throw new IllegalArgumentException("No RetryTaskProcessor registered for bizType '" + bizType + "'");
        }
        return processor;
    }
}
