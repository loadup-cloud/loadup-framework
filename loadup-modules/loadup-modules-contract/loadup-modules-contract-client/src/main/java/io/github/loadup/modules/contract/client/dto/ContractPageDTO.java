/*
 * #%L
 * LoadUp Contract
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
package io.github.loadup.modules.contract.client.dto;

import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record ContractPageDTO<T>(
        @Schema(description = "Entries on the current page") List<T> items,

        @Schema(description = "Total number of matching entries")
        long total,

        @Schema(description = "Page number, starting at 1", minimum = "1")
        int page,

        @Schema(description = "Page size; list queries allow at most 100", minimum = "1")
        int size) {
    public ContractPageDTO {
        items = List.copyOf(items);
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
