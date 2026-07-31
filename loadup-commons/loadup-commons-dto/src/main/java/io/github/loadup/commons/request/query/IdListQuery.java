package io.github.loadup.commons.request.query;

import io.github.loadup.commons.dto.DTO;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record IdListQuery(@NotBlank List<String> idList) implements DTO {

    public static IdListQuery of(List<String> idList) {
        return new IdListQuery(idList);
    }

    @Override
    public String toString() {
        return toJsonString();
    }
}
