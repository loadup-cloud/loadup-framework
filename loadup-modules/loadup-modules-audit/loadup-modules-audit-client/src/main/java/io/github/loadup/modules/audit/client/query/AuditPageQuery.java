/*
 * #%L
 * LoadUp Audit Client
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
package io.github.loadup.modules.audit.client.query;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.json.ToStringAsJson;
import io.github.loadup.modules.audit.client.error.AuditResultCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

public record AuditPageQuery(
        @Schema(description = "Actor identifier") String actorId,
        @Schema(description = "Action") String action,
        @Schema(description = "Outcome") String outcome,

        @Schema(description = "Inclusive start time in the application local time zone")
        LocalDateTime from,

        @Schema(description = "Inclusive end time in the application local time zone")
        LocalDateTime to,

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
    public AuditPageQuery {
        if (from != null && to != null && from.isAfter(to)) {
            throw new CommonException(AuditResultCodeEnum.INVALID_TIME_RANGE);
        }
        var pagination =
                io.github.loadup.commons.dto.PageQuery.normalized(page, size, AuditResultCodeEnum.INVALID_PAGE);
        page = pagination.page();
        size = pagination.size();
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
