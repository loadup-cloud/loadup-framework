package io.github.loadup.modules.transfer.web;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.authorization.model.LoadUpUser;
import io.github.loadup.modules.transfer.TransferKind;
import io.github.loadup.modules.transfer.TransferStatus;
import io.github.loadup.modules.transfer.TransferTask;
import io.github.loadup.modules.transfer.TransferTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authenticated submission and tracking of business-supplied import/export handlers. */
@RestController
@RequestMapping("/api/transfer-tasks")
@Tag(name = "Import Export Tasks", description = "Asynchronous import and export tasks")
public class TransferTaskController {
    private final TransferTaskService service;

    public TransferTaskController(TransferTaskService service) { this.service = service; }

    @PostMapping("/imports")
    @Operation(summary = "Submit an import using a previously uploaded file")
    @PreAuthorize("isAuthenticated()")
    public TransferView submitImport(@RequestBody SubmitRequest request, Authentication authentication) {
        if (request == null) throw new IllegalArgumentException("request is required");
        return TransferView.of(service.submit(TenantUtil.getTenantId(), actor(authentication), TransferKind.IMPORT,
                request.handlerKey(), request.sourceFileId(), request.options()));
    }

    @PostMapping("/exports")
    @Operation(summary = "Submit an export")
    @PreAuthorize("isAuthenticated()")
    public TransferView submitExport(@RequestBody SubmitRequest request, Authentication authentication) {
        if (request == null) throw new IllegalArgumentException("request is required");
        return TransferView.of(service.submit(TenantUtil.getTenantId(), actor(authentication), TransferKind.EXPORT,
                request.handlerKey(), request.sourceFileId(), request.options()));
    }

    @PostMapping("/list")
    @Operation(summary = "List my tasks; administrators may select an owner")
    @PreAuthorize("isAuthenticated()")
    public PageDTO<TransferView> list(@RequestBody ListRequest request, Authentication authentication) {
        var result = service.list(TenantUtil.getTenantId(), actor(authentication), admin(authentication),
                request.ownerId(), request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size());
        return PageDTO.of(result.records().stream().map(TransferView::of).toList(),
                result.total(), result.page(), result.size());
    }

    @PostMapping("/detail")
    @Operation(summary = "Get task progress and result metadata")
    @PreAuthorize("isAuthenticated()")
    public TransferView get(@RequestBody IdRequest request, Authentication authentication) {
        return TransferView.of(service.get(TenantUtil.getTenantId(), request.id(), actor(authentication), admin(authentication)));
    }

    @PostMapping("/result")
    @Operation(summary = "Get the result file reference for download")
    @PreAuthorize("isAuthenticated()")
    public ResultFile result(@RequestBody IdRequest request, Authentication authentication) {
        TransferTask task = service.get(TenantUtil.getTenantId(), request.id(), actor(authentication), admin(authentication));
        if (task.status() != TransferStatus.SUCCEEDED || task.resultFileId() == null) {
            throw new IllegalStateException("task has no result file");
        }
        return new ResultFile(task.resultFileId(), "/api/files/" + task.resultFileId() + "/content");
    }

    @PostMapping("/retry")
    @Operation(summary = "Redispatch a queued task or retry a failed task")
    @PreAuthorize("isAuthenticated()")
    public TransferView retry(@RequestBody IdRequest request, Authentication authentication) {
        return TransferView.of(service.retry(TenantUtil.getTenantId(), request.id(), actor(authentication), admin(authentication)));
    }

    private static String actor(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoadUpUser user)
                || user.getUserId() == null || user.getUserId().isBlank()) {
            throw new IllegalArgumentException("authenticated user is required");
        }
        return user.getUserId();
    }

    private static boolean admin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPER_ADMIN".equals(authority.getAuthority()));
    }

    public record SubmitRequest(String handlerKey, String sourceFileId, Map<String, String> options) {}
    public record IdRequest(String id) {}
    public record ListRequest(String ownerId, Integer page, Integer size) {}
    public record ResultFile(String fileId, String downloadPath) {}
    public record TransferView(String id, String ownerId, TransferKind kind, String handlerKey,
            String sourceFileId, String resultFileId, TransferStatus status, long processedCount, long totalCount,
            String errorMessage, LocalDateTime createdAt, LocalDateTime startedAt, LocalDateTime finishedAt) {
        static TransferView of(TransferTask task) {
            return new TransferView(task.id(), task.ownerId(), task.kind(), task.handlerKey(),
                    task.sourceFileId(), task.resultFileId(), task.status(), task.processedCount(),
                    task.totalCount(), task.errorMessage(), task.createdAt(), task.startedAt(), task.finishedAt());
        }
    }
}
