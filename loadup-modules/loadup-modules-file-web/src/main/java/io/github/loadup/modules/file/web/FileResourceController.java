package io.github.loadup.modules.file.web;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.authorization.model.LoadUpUser;
import io.github.loadup.modules.file.FileReference;
import io.github.loadup.modules.file.FileResource;
import io.github.loadup.modules.file.FileResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/** Authenticated file API; metadata uses the normal JSON envelope, content is streamed. */
@RestController
@RequestMapping("/api/files")
@Tag(name = "File Resources", description = "Managed file uploads, downloads and lifecycle")
public class FileResourceController {
    private final FileResourceService service;

    public FileResourceController(FileResourceService service) { this.service = service; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file")
    @PreAuthorize("isAuthenticated()")
    public FileResourceView upload(@RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        try (var content = file.getInputStream()) {
            return FileResourceView.of(service.upload(TenantUtil.getTenantId(), actor(authentication),
                    file.getOriginalFilename(), file.getContentType(), file.getSize(), content));
        }
    }

    @GetMapping
    @Operation(summary = "List my files; administrators may select an owner")
    @PreAuthorize("isAuthenticated()")
    public PageDTO<FileResourceView> list(@RequestParam(required = false) String ownerId,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        var result = service.list(TenantUtil.getTenantId(), actor(authentication), admin(authentication),
                ownerId, page, size);
        return PageDTO.of(result.records().stream().map(FileResourceView::of).toList(),
                result.total(), result.page(), result.size());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Read file metadata")
    @PreAuthorize("isAuthenticated()")
    public FileResourceView get(@PathVariable String id, Authentication authentication) {
        return FileResourceView.of(service.get(TenantUtil.getTenantId(), id,
                actor(authentication), admin(authentication)));
    }

    @GetMapping("/{id}/references")
    @Operation(summary = "List business references that protect a file from deletion")
    @PreAuthorize("isAuthenticated()")
    public List<FileReference> references(@PathVariable String id, Authentication authentication) {
        return service.references(TenantUtil.getTenantId(), id, actor(authentication), admin(authentication));
    }

    @GetMapping("/{id}/content")
    @Operation(summary = "Download file content")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable String id, Authentication authentication) {
        String tenant = TenantUtil.getTenantId();
        String actor = actor(authentication);
        boolean admin = admin(authentication);
        FileResource file = service.get(tenant, id, actor, admin);
        StreamingResponseBody body = output -> {
            try (var download = service.download(tenant, id, actor, admin)) {
                download.content().transferTo(output);
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.size())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.filename(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(body);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an unreferenced file; storage failures remain retryable")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<Void> delete(@PathVariable String id, Authentication authentication) {
        String tenant = TenantUtil.getTenantId();
        service.requestDeletion(tenant, id, actor(authentication), admin(authentication));
        service.cleanup(tenant, id);
        return SuccessResponse.success();
    }

    @PostMapping("/cleanup")
    @Operation(summary = "Retry pending DFS deletions")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public int cleanup(@RequestParam(defaultValue = "100") int limit) {
        return service.cleanupPending(limit);
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

    /** Public metadata omits the internal DFS storage key and tenant identifier. */
    public record FileResourceView(String id, String ownerId, String filename, String contentType,
            long size, String provider, LocalDateTime createdAt, LocalDateTime updatedAt) {
        static FileResourceView of(FileResource file) {
            return new FileResourceView(file.id(), file.ownerId(), file.filename(), file.contentType(),
                    file.size(), file.provider(), file.createdAt(), file.updatedAt());
        }
    }
}
