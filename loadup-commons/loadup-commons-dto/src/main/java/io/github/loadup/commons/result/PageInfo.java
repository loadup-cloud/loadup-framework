/*
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
package io.github.loadup.commons.result;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Pagination metadata")
public record PageInfo(
        @Schema(description = "Total record count") Long totalCount,
        @Schema(description = "Page size") Long pageSize,
        @Schema(description = "Page number, starting at 1") Long pageIndex) {
    public PageInfo {
        if (totalCount == null
                || totalCount < 0
                || pageSize == null
                || pageSize < 1
                || pageIndex == null
                || pageIndex < 1)
            throw new io.github.loadup.commons.error.CommonException(
                    io.github.loadup.commons.enums.CommonResultCodeEnum.PARAM_ILLEGAL);
    }

    @Schema(description = "Total page count")
    public Long getTotalPages() {
        if (pageSize == null || pageSize <= 0) return 0L;
        return totalCount == 0 ? 0L : 1 + (totalCount - 1) / pageSize;
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
