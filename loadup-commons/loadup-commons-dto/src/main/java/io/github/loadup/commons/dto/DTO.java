package io.github.loadup.commons.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;

/**
 * Marker interface for DTOs providing standard Jackson JSON serialization for {@link #toString()}.
 */
public interface DTO extends Serializable {

    ObjectMapper MAPPER = new ObjectMapper();

    default String toJsonString() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (Exception e) {
            return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
        }
    }
}
