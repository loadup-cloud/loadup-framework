package io.github.loadup.commons.result;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Contract for result codes carried by {@link Result} and result enums. */
public interface ResultCode {
    @NotBlank
    @Size(max = 64)
    String getCode();

    @NotBlank
    @Size(max = 2)
    String getStatus();

    @NotBlank
    @Size(max = 256)
    String getMessage();
}
