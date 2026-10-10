/*
 * #%L
 * LoadUp File Resources Web Adapter
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
package io.github.loadup.modules.file.web;

import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.authorization.model.LoadUpUser;
import io.github.loadup.modules.file.client.command.FileCleanupRequest;
import io.github.loadup.modules.file.client.command.FileIdRequest;
import io.github.loadup.modules.file.client.dto.FileReferenceDTO;
import io.github.loadup.modules.file.client.dto.FileResourceDTO;
import io.github.loadup.modules.file.client.dto.FileResourceViewDTO;
import io.github.loadup.modules.file.client.facade.FileResourceFacade;
import io.github.loadup.modules.file.client.query.FileListRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/** Authenticated file API; metadata uses the normal JSON envelope, content is streamed. */
@RestController
@RequestMapping("/files")
@Tag(name = "File Resources", description = "Managed file uploads, downloads and lifecycle")
public class FileResourceController {
    private final FileWebConverter converter;
    private final FileResourceFacade service;

    public FileResourceController(FileResourceFacade service, FileWebConverter converter) {
        this.converter = converter;
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<FileResourceViewDTO> upload(
            @RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        try (var content = file.getInputStream()) {
            return SuccessResponse.of(converter.toView(service.upload(
                    TenantUtil.getTenantId(),
                    actor(authentication),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    content)));
        }
    }

    @PostMapping("/list")
    @Operation(summary = "List my files; administrators may select an owner")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<java.util.Collection<FileResourceViewDTO>> list(
            @RequestBody FileListRequest request, Authentication authentication) {
        var result = service.list(
                TenantUtil.getTenantId(),
                actor(authentication),
                admin(authentication),
                request.ownerId(),
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size());
        return SuccessResponse.ofPage(PageDTO.of(
                result.records().stream().map(converter::toView).toList(),
                result.total(),
                result.page(),
                result.size()));
    }

    @PostMapping("/detail")
    @Operation(summary = "Read file metadata")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<FileResourceViewDTO> get(@RequestBody FileIdRequest request, Authentication authentication) {
        return SuccessResponse.of(converter.toView(
                service.get(TenantUtil.getTenantId(), request.id(), actor(authentication), admin(authentication))));
    }

    @PostMapping("/references")
    @Operation(summary = "List business references that protect a file from deletion")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<List<FileReferenceDTO>> references(
            @RequestBody FileIdRequest request, Authentication authentication) {
        return SuccessResponse.of(service.references(
                TenantUtil.getTenantId(), request.id(), actor(authentication), admin(authentication)));
    }

    @GetMapping("/{id}/content")
    @Operation(summary = "Download file content")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable String id, Authentication authentication) {
        String tenant = TenantUtil.getTenantId();
        String actor = actor(authentication);
        boolean admin = admin(authentication);
        FileResourceDTO file = service.get(tenant, id, actor, admin);
        StreamingResponseBody body = output -> {
            try (var download = service.download(tenant, id, actor, admin)) {
                download.content().transferTo(output);
            }
        };
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.size())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(file.filename(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(body);
    }

    @PostMapping("/delete")
    @Operation(summary = "Delete an unreferenced file; storage failures remain retryable")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<Void> delete(@RequestBody FileIdRequest request, Authentication authentication) {
        String tenant = TenantUtil.getTenantId();
        service.requestDeletion(tenant, request.id(), actor(authentication), admin(authentication));
        service.cleanup(tenant, request.id());
        return SuccessResponse.success();
    }

    @PostMapping("/cleanup")
    @Operation(summary = "Retry pending DFS deletions")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public SuccessResponse<Integer> cleanup(@RequestBody FileCleanupRequest request) {
        return SuccessResponse.of(service.cleanupPending(request.limit() == null ? 100 : request.limit()));
    }

    private static String actor(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof LoadUpUser user)
                || user.getUserId() == null
                || user.getUserId().isBlank()) {
            throw new IllegalArgumentException("authenticated user is required");
        }
        return user.getUserId();
    }

    private static boolean admin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_SUPER_ADMIN".equals(authority.getAuthority()));
    }

    /** Public metadata omits the internal DFS storage key and tenant identifier. */
}
