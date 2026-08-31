/*-
 * #%L
 * Loadup Common DTO
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
package io.github.loadup.commons.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Marker interface for DTOs providing standard Jackson JSON serialization for {@link #toString()}.
 *
 * <p>The {@link ObjectMapper} used for serialization is externally configurable
 * via {@link #setObjectMapper(ObjectMapper)}. Spring Boot applications should
 * inject the auto-configured {@code ObjectMapper} bean to ensure consistent
 * serialization format (date patterns, naming strategies, modules, etc.).
 *
 * <p>If no mapper is set, a default {@code new ObjectMapper()} is used.
 */
public interface DTO extends Serializable {

    AtomicReference<ObjectMapper> MAPPER = new AtomicReference<>(new ObjectMapper());

    /**
     * Replace the default ObjectMapper with a globally configured one.
     * Typically called at startup by Spring auto-configuration.
     */
    static void setObjectMapper(ObjectMapper mapper) {
        MAPPER.set(mapper);
    }

    /**
     * Serialize this object to standard JSON via the configured ObjectMapper.
     * Falls back to {@code ClassName@hashCode} if serialization fails.
     */
    default String toJsonString() {
        try {
            return MAPPER.get().writeValueAsString(this);
        } catch (Exception e) {
            return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
        }
    }
}
