/*
 * #%L
 * LoadUp Import Export Tasks Web Adapter
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
package io.github.loadup.modules.transfer.web;

import io.github.loadup.commons.request.query.IdQuery;
import io.github.loadup.commons.result.PageResponse;
import io.github.loadup.commons.result.SuccessResponse;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.authorization.model.LoadUpUser;
import io.github.loadup.modules.transfer.client.command.TransferTaskRetryCommand;
import io.github.loadup.modules.transfer.client.command.TransferTaskSubmitCommand;
import io.github.loadup.modules.transfer.client.dto.ResultFileDTO;
import io.github.loadup.modules.transfer.client.dto.TransferTaskDTO;
import io.github.loadup.modules.transfer.client.dto.TransferViewDTO;
import io.github.loadup.modules.transfer.client.enums.TransferKind;
import io.github.loadup.modules.transfer.client.enums.TransferStatus;
import io.github.loadup.modules.transfer.client.facade.TransferTaskFacade;
import io.github.loadup.modules.transfer.client.query.TransferTaskPageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Authenticated submission and tracking of business-supplied import/export handlers. */
@RestController
@RequestMapping("/transfer-tasks")
@Tag(name = "Import Export Tasks", description = "Asynchronous import and export tasks")
public class TransferTaskController {
    private final TransferWebConverter converter;
    private final TransferTaskFacade service;

    public TransferTaskController(TransferTaskFacade service, TransferWebConverter converter) {
        this.converter = converter;
        this.service = service;
    }

    @PostMapping("/imports")
    @Operation(summary = "Submit an import using a previously uploaded file")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<TransferViewDTO> submitImport(
            @RequestBody TransferTaskSubmitCommand request, Authentication authentication) {
        if (request == null) throw new IllegalArgumentException("request is required");
        return SuccessResponse.of(converter.toView(service.submit(
                TenantUtil.getTenantId(),
                actor(authentication),
                TransferKind.IMPORT,
                request.handlerKey(),
                request.sourceFileId(),
                request.options())));
    }

    @PostMapping("/exports")
    @Operation(summary = "Submit an export")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<TransferViewDTO> submitExport(
            @RequestBody TransferTaskSubmitCommand request, Authentication authentication) {
        if (request == null) throw new IllegalArgumentException("request is required");
        return SuccessResponse.of(converter.toView(service.submit(
                TenantUtil.getTenantId(),
                actor(authentication),
                TransferKind.EXPORT,
                request.handlerKey(),
                request.sourceFileId(),
                request.options())));
    }

    @PostMapping("/list")
    @Operation(summary = "List my tasks; administrators may select an owner")
    @PreAuthorize("isAuthenticated()")
    public PageResponse<TransferViewDTO> list(
            @RequestBody TransferTaskPageQuery request, Authentication authentication) {
        var result = service.list(
                TenantUtil.getTenantId(),
                actor(authentication),
                admin(authentication),
                request.ownerId(),
                request.page() == null ? 1 : request.page(),
                request.size() == null ? 20 : request.size());
        return PageResponse.of(result.map(converter::toView));
    }

    @PostMapping("/detail")
    @Operation(summary = "Get task progress and result metadata")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<TransferViewDTO> get(@RequestBody IdQuery request, Authentication authentication) {
        return SuccessResponse.of(converter.toView(
                service.get(TenantUtil.getTenantId(), request.id(), actor(authentication), admin(authentication))));
    }

    @PostMapping("/result")
    @Operation(summary = "Get the result file reference for download")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<ResultFileDTO> result(@RequestBody IdQuery request, Authentication authentication) {
        TransferTaskDTO task =
                service.get(TenantUtil.getTenantId(), request.id(), actor(authentication), admin(authentication));
        if (task.status() != TransferStatus.SUCCEEDED || task.resultFileId() == null) {
            throw new IllegalStateException("task has no result file");
        }
        return SuccessResponse.of(
                new ResultFileDTO(task.resultFileId(), "/api/files/" + task.resultFileId() + "/content"));
    }

    @PostMapping("/retry")
    @Operation(summary = "Redispatch a queued task or retry a failed task")
    @PreAuthorize("isAuthenticated()")
    public SuccessResponse<TransferViewDTO> retry(
            @RequestBody TransferTaskRetryCommand request, Authentication authentication) {
        return SuccessResponse.of(converter.toView(
                service.retry(TenantUtil.getTenantId(), request.id(), actor(authentication), admin(authentication))));
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
}
