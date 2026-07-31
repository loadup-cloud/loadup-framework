package io.github.loadup.commons.request.query;

import io.github.loadup.commons.dto.DTO;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public record IdQuery(@NotBlank String id) implements DTO {

    public static IdQuery of(String id) {
        return new IdQuery(Objects.requireNonNull(id, "id must not be null"));
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
