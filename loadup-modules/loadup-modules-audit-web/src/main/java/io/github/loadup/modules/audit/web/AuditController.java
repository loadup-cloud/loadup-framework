package io.github.loadup.modules.audit.web;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.modules.audit.AuditEvent;
import io.github.loadup.modules.audit.AuditPage;
import io.github.loadup.modules.audit.AuditQuery;
import io.github.loadup.modules.audit.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Administrator-only audit search. */
@RestController
@Tag(name = "Audit", description = "Tenant-scoped administrator audit search")
public class AuditController {
    private final AuditService service;

    public AuditController(AuditService service) {
        this.service = service;
    }

    @PostMapping("/audit/events/query")
    @Operation(summary = "Search audit events")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public PageDTO<AuditEvent> query(@RequestBody AuditSearchRequest request) {
        AuditPage result = service.search(new AuditQuery(
                TenantUtil.getTenantId(),
                request.actorId(),
                request.action(),
                request.outcome(),
                request.from(),
                request.to(),
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size()));
        return PageDTO.of(result.events(), result.total(), result.page(), result.size());
    }

    public record AuditSearchRequest(
            String actorId,
            String action,
            String outcome,
            LocalDateTime from,
            LocalDateTime to,
            Integer page,
            Integer size) {}
}
