package io.github.loadup.retrytask.core;

/*-
 * #%L
 * Loadup Components Retrytask Core
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for {@link RetryTaskProcessor}s
 */
public class RetryTaskProcessorRegistry {

    private final Map<String, RetryTaskProcessor> processors = new ConcurrentHashMap<>();

    public RetryTaskProcessorRegistry(List<RetryTaskProcessor> processorList) {
        for (RetryTaskProcessor processor : processorList) {
            processors.put(processor.getBizType(), processor);
        }
    }

    /**
     * Gets the processor for the given business type.
     *
     * @param bizType The business type.
     * @return The processor, or {@code null} if no processor is found.
     */
    public RetryTaskProcessor getProcessor(String bizType) {
        return processors.get(bizType);
    }
}
