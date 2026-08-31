/*-
 * #%L
 * Loadup Components Retrytask Binder JobRunr
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
