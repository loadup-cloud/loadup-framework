/*
 * #%L
 * LoadUp Audit App
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
package io.github.loadup.modules.audit.app.converter;

import io.github.loadup.commons.mapping.LoadUpMapStructConfig;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.modules.audit.client.dto.AuditEventDTO;
import io.github.loadup.modules.audit.domain.model.AuditEvent;
import io.github.loadup.modules.audit.domain.model.AuditPage;
import io.github.loadup.modules.audit.domain.model.AuditQuery;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps application results without exposing persistence or domain models. */
@Mapper(config = LoadUpMapStructConfig.class)
public interface AuditDTOConverter {
    AuditEventDTO toDTO(AuditEvent value);

    @Mapping(target = "data", source = "events")
    @Mapping(target = "pageInfo.totalCount", source = "total")
    @Mapping(target = "pageInfo.pageIndex", source = "page")
    @Mapping(target = "pageInfo.pageSize", source = "size")
    PageDTO<AuditEventDTO> toPageDTO(AuditPage value);

    AuditQuery toDomain(io.github.loadup.modules.audit.client.query.AuditQuery value);
}
