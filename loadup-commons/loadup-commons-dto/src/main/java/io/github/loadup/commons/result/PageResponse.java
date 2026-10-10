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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.Objects;

/** Standard successful paginated API envelope. */
@Schema(description = "Successful paginated response")
@JsonPropertyOrder({"result", "data", "pageInfo"})
public record PageResponse<T>(
        @Schema(description = "Result metadata") Result result,

        @Schema(description = "Entries on the current page") @JsonInclude(JsonInclude.Include.ALWAYS)
        Collection<T> data,

        @Schema(description = "Pagination metadata") PageInfo pageInfo)
        implements IResponse<Collection<T>> {

    public PageResponse {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(pageInfo, "pageInfo");
    }

    public static <T> PageResponse<T> of(PageDTO<T> page) {
        return new PageResponse<>(Result.buildSuccess(), page.getData(), page.getPageInfo());
    }

    @Override
    public Result getResult() {
        return result;
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
