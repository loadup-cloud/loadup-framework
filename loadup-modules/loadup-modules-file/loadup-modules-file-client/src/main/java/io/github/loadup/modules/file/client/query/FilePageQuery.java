/*
 * #%L
 * LoadUp File Client
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
package io.github.loadup.modules.file.client.query;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record FilePageQuery(
        @Schema(description = "Owner user identifier") String ownerId,

        @Schema(description = "Page number, starting at 1", minimum = "1", defaultValue = "1") @Min(1)
        Integer page,

        @Schema(
                description = "Page size; list queries allow at most 100",
                minimum = "1",
                maximum = "100",
                defaultValue = "20")
        @Min(1)
        @Max(100)
        Integer size) {
    public FilePageQuery {
        page = page == null ? 1 : page;
        size = size == null ? 20 : size;
        if (page < 1 || size < 1 || size > 100) {
            throw new io.github.loadup.commons.error.CommonException(
                    io.github.loadup.commons.enums.CommonResultCodeEnum.PARAM_ILLEGAL,
                    "Page must be positive and size must be between 1 and 100");
        }
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
