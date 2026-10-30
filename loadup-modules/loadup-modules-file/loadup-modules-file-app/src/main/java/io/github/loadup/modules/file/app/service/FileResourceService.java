/*
 * #%L
 * LoadUp File App
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
package io.github.loadup.modules.file.app.service;

import io.github.loadup.commons.constant.CommonConstants;
import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.commons.util.TenantUtil;
import io.github.loadup.components.dfs.DfsService;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import io.github.loadup.modules.file.app.converter.FileDTOConverter;
import io.github.loadup.modules.file.client.dto.FileReferenceDTO;
import io.github.loadup.modules.file.client.dto.FileResourceDTO;
import io.github.loadup.modules.file.client.error.FileResultCodeEnum;
import io.github.loadup.modules.file.domain.gateway.FileResourceGateway;
import io.github.loadup.modules.file.domain.model.FileReference;
import io.github.loadup.modules.file.domain.model.FileResource;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/**
 * File lifecycle and business references; DFS remains responsible for bytes.
 */
public class FileResourceService implements io.github.loadup.modules.file.client.facade.FileResourceFacade {
    private final Clock clock;
    private final FileDTOConverter converter;
    private final FileResourceGateway repository;
    private final DfsService dfs;

    public FileResourceService(
            FileResourceGateway repository, DfsService dfs, FileDTOConverter converter, Clock clock) {
        this.clock = clock;
        this.converter = converter;
        this.repository = repository;
        this.dfs = dfs;
    }

    private static void authorize(FileResource file, String actorId, boolean admin) {
        if (!admin && !file.ownerId().equals(actorId)) {
            throw new CommonException(FileResultCodeEnum.FILE_NOT_FOUND);
        }
    }

    private static String storageTenant() {
        String tenant = TenantUtil.getTenantId();
        return tenant == null || tenant.isBlank() ? CommonConstants.DEFAULT_TENANT_ID : tenant.trim();
    }

    private static String key(String value) {
        String result = required(value, 128);
        if (!result.matches("[A-Za-z0-9_.:-]+")) {
            throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
        }
        return result;
    }

    private static String required(String value, int max) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
        }
        return value.trim();
    }

    public FileResourceDTO upload(String ownerId, String filename, String contentType, long size, InputStream content) {
        String owner = required(ownerId, 64);
        String safeName = required(filename, 255);
        if (safeName.contains("/")
                || safeName.contains("\\")
                || safeName.chars().anyMatch(Character::isISOControl)) {
            throw new CommonException(FileResultCodeEnum.FILENAME_CONTAINS_INVALID_CHARACTERS);
        }
        if (content == null || size < 0) {
            throw new CommonException(FileResultCodeEnum.INVALID_FILE_CONTENT);
        }
        String type =
                contentType == null || contentType.isBlank() ? "application/octet-stream" : required(contentType, 255);
        FileMetadata stored = TenantUtil.callWithTenant(
                storageTenant(),
                () -> dfs.upload(new FileUploadRequest(
                        safeName, content, size, type, "resources/" + UUID.randomUUID(), Map.of())));
        LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), clock.getZone());
        FileResource file = new FileResource(
                UUID.randomUUID().toString(),
                owner,
                stored.fileId(),
                safeName,
                type,
                stored.size(),
                stored.provider(),
                "ACTIVE",
                now,
                now);
        try {
            repository.insert(file);
        } catch (RuntimeException failure) {
            try {
                TenantUtil.runWithTenant(storageTenant(), () -> dfs.delete(stored.fileId()));
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
        return converter.toDTO(file);
    }

    public FileResourceDTO get(String id, String actorId, boolean admin) {
        return converter.toDTO(requireFile(id, actorId, admin));
    }

    private FileResource requireFile(String id, String actorId, boolean admin) {
        FileResource file = repository
                .find(required(id, 64))
                .filter(item -> "ACTIVE".equals(item.state()))
                .orElseThrow(() -> new CommonException(FileResultCodeEnum.FILE_NOT_FOUND));
        authorize(file, actorId, admin);
        return file;
    }

    public FileDownloadResponse download(String id, String actorId, boolean admin) {
        FileResource file = requireFile(id, actorId, admin);
        return TenantUtil.callWithTenant(storageTenant(), () -> dfs.download(file.storageId()));
    }

    public PageDTO<FileResourceDTO> list(String actorId, boolean admin, String ownerId, int page, int size) {
        io.github.loadup.commons.dto.PageQuery.normalized(page, size, FileResultCodeEnum.INVALID_PAGE);
        String owner = admin && ownerId != null && !ownerId.isBlank() ? required(ownerId, 64) : required(actorId, 64);
        return converter.toPageDTO(repository.list(owner, page, size));
    }

    @Transactional
    public FileReferenceDTO attach(String id, String actorId, boolean admin, String referenceType, String referenceId) {
        FileResource file = lockActive(id, actorId, admin);
        FileReference reference = new FileReference(
                UUID.randomUUID().toString(),
                file.id(),
                key(referenceType),
                key(referenceId),
                LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
        repository.addReference(reference);
        return converter.toDTO(reference);
    }

    @Transactional
    public void detach(String id, String actorId, boolean admin, String referenceType, String referenceId) {
        lockActive(id, actorId, admin);
        repository.removeReference(id, key(referenceType), key(referenceId));
    }

    public List<FileReferenceDTO> references(String id, String actorId, boolean admin) {
        FileResource file = requireFile(id, actorId, admin);
        return converter.toReferenceDTOs(repository.references(file.id()));
    }

    /**
     * Commit the pending state before calling DFS; retry cleanupPending after storage failure.
     */
    @Transactional
    public void requestDeletion(String id, String actorId, boolean admin) {
        FileResource file = lockActive(id, actorId, admin);
        if (repository.countReferences(file.id()) > 0) {
            throw new CommonException(FileResultCodeEnum.FILE_IS_REFERENCED_BY_A_BUSINESS_OBJECT);
        }
        repository.markPending(file.id());
    }

    /**
     * Idempotent cleanup entry point for operators or a scheduler.
     */
    public int cleanupPending(int limit) {
        if (limit < 1 || limit > 1000) {
            throw new CommonException(FileResultCodeEnum.LIMIT_MUST_BE_BETWEEN_1_AND_1000);
        }
        int cleaned = 0;
        RuntimeException failure = null;
        for (FileResource file : repository.pending(limit)) {
            try {
                TenantUtil.runWithTenant(storageTenant(), () -> {
                    dfs.delete(file.storageId());
                    repository.markDeleted(file.id());
                });
                cleaned++;
            } catch (RuntimeException ex) {
                if (failure == null) {
                    failure = new CommonException(FileResultCodeEnum.SOME_PENDING_FILE_DELETIONS_FAILED);
                }
                failure.addSuppressed(ex);
            }
        }
        if (failure != null) {
            throw failure;
        }
        return cleaned;
    }

    public void cleanup(String id) {
        FileResource file = repository
                .find(required(id, 64))
                .filter(item -> "PENDING_DELETE".equals(item.state()))
                .orElseThrow(() -> new CommonException(FileResultCodeEnum.FILE_IS_NOT_PENDING_DELETION));
        TenantUtil.runWithTenant(storageTenant(), () -> dfs.delete(file.storageId()));
        repository.markDeleted(file.id());
    }

    private FileResource lockActive(String id, String actorId, boolean admin) {
        FileResource file = repository
                .lock(required(id, 64))
                .filter(item -> "ACTIVE".equals(item.state()))
                .orElseThrow(() -> new CommonException(FileResultCodeEnum.FILE_NOT_FOUND));
        authorize(file, actorId, admin);
        return file;
    }
}
