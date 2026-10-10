/*
 * #%L
 * LoadUp Audit Center Web Adapter
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
package io.github.loadup.modules.audit.web;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.modules.audit.client.dto.AuditEventDTO;
import io.github.loadup.modules.audit.client.dto.AuditPageDTO;
import io.github.loadup.modules.audit.client.facade.AuditFacade;
import io.github.loadup.modules.audit.client.query.AuditQuery;
import io.github.loadup.modules.audit.client.query.AuditSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Administrator-only audit search. */
@RestController
@Tag(name = "Audit", description = "Tenant-scoped administrator audit search")
public class AuditController {
    private final AuditFacade service;

    public AuditController(AuditFacade service) {
        this.service = service;
    }

    @PostMapping("/audit/events/query")
    @Operation(summary = "Search audit events")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<java.util.Collection<AuditEventDTO>> query(@RequestBody AuditSearchRequest request) {
        AuditPageDTO result = service.search(new AuditQuery(
                TenantUtil.getTenantId(),
                request.actorId(),
                request.action(),
                request.outcome(),
                request.from(),
                request.to(),
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size()));
        return SuccessResponse.ofPage(PageDTO.of(result.events(), result.total(), result.page(), result.size()));
    }
}
