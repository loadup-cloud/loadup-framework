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
