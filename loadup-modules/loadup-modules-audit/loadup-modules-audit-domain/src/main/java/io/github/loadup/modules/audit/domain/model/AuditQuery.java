/*
 * #%L
 * LoadUp Audit Domain
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
package io.github.loadup.modules.audit.domain.model;

import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.json.ToStringAsJson;
import io.github.loadup.modules.audit.client.error.AuditResultCodeEnum;
import java.time.LocalDateTime;

/**
 * Audit search scoped to the current schema.
 */
public record AuditQuery(
        String actorId, String action, String outcome, LocalDateTime from, LocalDateTime to, int page, int size) {
    public AuditQuery {
        io.github.loadup.commons.dto.PageQuery.normalized(page, size, AuditResultCodeEnum.INVALID_PAGE);
        if (from != null && to != null && from.isAfter(to)) {
            throw new CommonException(AuditResultCodeEnum.INVALID_TIME_RANGE);
        }
    }

    @Override
    public String toString() {
        return ToStringAsJson.reflectionToString(this);
    }
}
