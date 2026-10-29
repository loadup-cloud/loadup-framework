/*
 * #%L
 * LoadUp Transfer App
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
package io.github.loadup.modules.transfer.app.service;

import io.github.loadup.commons.enums.CommonResultCodeEnum;
import io.github.loadup.commons.error.CommonException;
import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.commons.result.PageDTO;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.modules.file.client.dto.FileResourceDTO;
import io.github.loadup.modules.file.client.facade.FileResourceFacade;
import io.github.loadup.modules.transfer.app.config.TransferTaskProperties;
import io.github.loadup.modules.transfer.app.converter.TransferDTOConverter;
import io.github.loadup.modules.transfer.client.dto.TransferTaskDTO;
import io.github.loadup.modules.transfer.client.error.TransferResultCodeEnum;
import io.github.loadup.modules.transfer.client.spi.TransferContext;
import io.github.loadup.modules.transfer.client.spi.TransferHandler;
import io.github.loadup.modules.transfer.domain.gateway.TransferGateway;
import io.github.loadup.modules.transfer.domain.model.TransferKind;
import io.github.loadup.modules.transfer.domain.model.TransferStatus;
import io.github.loadup.modules.transfer.domain.model.TransferTask;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;

/** Persistent task service for bounded file transfers. */
public class TransferTaskService implements io.github.loadup.modules.transfer.client.facade.TransferTaskFacade {
    private final Clock clock;

    private static final String REF_TYPE = "transfer-task";
    private static final String DEFAULT_TENANT = "__default__";

    private final TransferDTOConverter converter;
    private final TransferGateway repository;
    private final FileResourceFacade files;
    private final Map<String, TransferHandler> handlers;
    private final TransferTaskProperties properties;

    public TransferTaskService(
            TransferGateway repository,
            FileResourceFacade files,
            List<TransferHandler> handlers,
            TransferTaskProperties properties,
            TransferDTOConverter converter,
            @Qualifier("transferClock") Clock clock) {
        this.clock = clock;
        this.converter = converter;
        this.repository = repository;
        this.files = files;
        this.properties = properties;
        this.handlers = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        handler -> handler.kind().name() + ":" + handler.key(), Function.identity()));
    }

    @org.springframework.transaction.annotation.Transactional
    public TransferTaskDTO submit(
            String tenantId,
            String ownerId,
            io.github.loadup.modules.transfer.client.enums.TransferKind kind,
            String handlerKey,
            String sourceFileId,
            Map<String, String> options) {
        String tenant = tenant(tenantId);
        String owner = required(ownerId, 64);
        TransferHandler handler = handler(TransferKind.valueOf(kind.name()), handlerKey);
        Map<String, String> args = options(options);
        if (kind == io.github.loadup.modules.transfer.client.enums.TransferKind.IMPORT) {
            FileResourceDTO source = files.get(tenant, required(sourceFileId, 64), owner, false);
            if (source.size() > properties.getMaxInputBytes()) {
                throw new CommonException(TransferResultCodeEnum.SOURCE_FILE_EXCEEDS_IMPORT_LIMIT);
            }
        } else if (sourceFileId != null) {
            throw new CommonException(TransferResultCodeEnum.EXPORT_SOURCE_FORBIDDEN);
        }
        String id = UUID.randomUUID().toString();
        TransferTask task = new TransferTask(
                id,
                tenant,
                owner,
                TransferKind.valueOf(handler.kind().name()),
                handler.key(),
                sourceFileId,
                null,
                TransferStatus.QUEUED,
                0,
                0,
                null,
                LocalDateTime.ofInstant(clock.instant(), clock.getZone()),
                null,
                null);
        protectSource(task);
        repository.insert(task, args);
        return converter.toDTO(repository.find(id).orElseThrow());
    }

    public TransferTaskDTO get(String tenantId, String id, String actorId, boolean admin) {
        return converter.toDTO(requireTask(tenantId, id, actorId, admin));
    }

    private TransferTask requireTask(String tenantId, String id, String actorId, boolean admin) {
        TransferTask task = repository
                .find(required(id, 64))
                .filter(item -> item.tenantId().equals(tenant(tenantId)))
                .orElseThrow(() -> new CommonException(TransferResultCodeEnum.TASK_NOT_FOUND));
        if (!admin && !task.ownerId().equals(actorId)) throw new CommonException(TransferResultCodeEnum.TASK_NOT_FOUND);
        return task;
    }

    public PageDTO<TransferTaskDTO> list(
            String tenantId, String actorId, boolean admin, String ownerId, int page, int size) {
        io.github.loadup.commons.dto.PageQuery.normalized(page, size, TransferResultCodeEnum.INVALID_PAGE);
        String owner = admin && ownerId != null && !ownerId.isBlank() ? required(ownerId, 64) : required(actorId, 64);
        return converter.toPageDTO(repository.list(tenant(tenantId), owner, page, size));
    }

    /** Redispatch a queued task or explicitly replay a failed task. */
    @org.springframework.transaction.annotation.Transactional
    public TransferTaskDTO retry(String tenantId, String id, String actorId, boolean admin) {
        TransferTask task = requireTask(tenantId, id, actorId, admin);
        if (task.status() != TransferStatus.FAILED && task.status() != TransferStatus.QUEUED) {
            throw new CommonException(TransferResultCodeEnum.TASK_NOT_DISPATCHABLE);
        }
        if (task.status() == TransferStatus.QUEUED) return converter.toDTO(task);
        if (task.sourceFileId() != null) {
            files.get(task.tenantId(), task.sourceFileId(), task.ownerId(), false);
        }
        if (!repository.requeue(task.tenantId(), task.id())) {
            throw new CommonException(TransferResultCodeEnum.TASK_STATUS_CHANGED);
        }
        protectSource(task);
        return converter.toDTO(repository.find(task.id()).orElseThrow());
    }

    /** Drains a bounded snapshot; atomic claims prevent duplicate work across worker instances. */
    public boolean executeQueuedTasks() {
        boolean succeeded = true;
        for (String id : repository.queued(properties.getDispatchBatchSize())) {
            try {
                execute(id);
            } catch (Exception failure) {
                LogUtil.warn(
                        TransferTaskService.class,
                        "Transfer dispatch failed taskId={} type={}",
                        id,
                        failure.getClass().getSimpleName());
                succeeded = false;
            }
        }
        return succeeded;
    }

    private void execute(String id) throws Exception {
        TransferTask task =
                repository.find(id).orElseThrow(() -> new CommonException(TransferResultCodeEnum.TASK_NOT_FOUND));
        io.github.loadup.commons.util.TenantUtil.callWithTenant(task.tenantId(), () -> {
            executeForTenant(task);
            return null;
        });
    }

    private void executeForTenant(TransferTask task) throws Exception {
        String id = task.id();
        if (!repository.start(task.tenantId(), id)) return;
        Path output = null;
        String uploadedId = null;
        try {
            TransferHandler handler = handler(task.kind(), task.handlerKey());
            output = Files.createTempFile("loadup-transfer-", ".tmp");
            TransferContext ctx = new TransferContext(
                    task.id(),
                    task.tenantId(),
                    task.ownerId(),
                    repository.options(task.tenantId(), id),
                    (processed, total) -> report(task.tenantId(), id, processed, total));
            try (FileDownloadResponse source = task.kind() == TransferKind.IMPORT
                            ? files.download(task.tenantId(), task.sourceFileId(), task.ownerId(), false)
                            : null;
                    OutputStream sink =
                            new BoundedOutputStream(Files.newOutputStream(output), properties.getMaxOutputBytes())) {
                handler.process(ctx, source == null ? InputStream.nullInputStream() : source.content(), sink);
            }
            long length = Files.size(output);
            if (task.kind() == TransferKind.EXPORT || length > 0) {
                try (InputStream generated = Files.newInputStream(output)) {
                    uploadedId = files.upload(
                                    task.tenantId(),
                                    task.ownerId(),
                                    handler.outputFilename(),
                                    handler.outputContentType(),
                                    length,
                                    generated)
                            .id();
                }
            }
            repository.succeed(task.tenantId(), id, uploadedId);
            releaseSource(task);
        } catch (Exception failure) {
            if (uploadedId != null) deleteUnlinkedOutput(task, uploadedId);
            releaseSource(task);
            try {
                repository.fail(task.tenantId(), id, "Task processing failed");
            } catch (RuntimeException stateFailure) {
                failure.addSuppressed(stateFailure);
            }
            LogUtil.warn(TransferTaskService.class, "Transfer task {} failed", id, failure);
            throw failure;
        } finally {
            if (output != null) {
                try {
                    Files.deleteIfExists(output);
                } catch (IOException cleanupFailure) {
                    LogUtil.warn(
                            TransferTaskService.class,
                            "Unable to remove temporary output for task {}",
                            id,
                            cleanupFailure);
                }
            }
        }
    }

    private void report(String tenant, String id, long processed, long total) {
        if (processed < 0 || total < processed) throw new CommonException(TransferResultCodeEnum.INVALID_TASK_PROGRESS);
        repository.progress(tenant, id, processed, total);
    }

    private void protectSource(TransferTask task) {
        if (task.sourceFileId() == null) return;
        boolean alreadyLinked = files.references(task.tenantId(), task.sourceFileId(), task.ownerId(), false).stream()
                .anyMatch(
                        ref -> REF_TYPE.equals(ref.referenceType()) && task.id().equals(ref.referenceId()));
        if (!alreadyLinked)
            files.attach(task.tenantId(), task.sourceFileId(), task.ownerId(), false, REF_TYPE, task.id());
    }

    private void releaseSource(TransferTask task) {
        if (task.sourceFileId() == null) return;
        try {
            files.detach(task.tenantId(), task.sourceFileId(), task.ownerId(), false, REF_TYPE, task.id());
        } catch (RuntimeException cleanupFailure) {
            LogUtil.warn(
                    TransferTaskService.class,
                    "Unable to release source reference for task {}",
                    task.id(),
                    cleanupFailure);
        }
    }

    private void deleteUnlinkedOutput(TransferTask task, String fileId) {
        try {
            files.requestDeletion(task.tenantId(), fileId, task.ownerId(), false);
            files.cleanup(task.tenantId(), fileId);
        } catch (RuntimeException cleanupFailure) {
            LogUtil.warn(
                    TransferTaskService.class,
                    "Unable to remove orphan output {} for task {}",
                    fileId,
                    task.id(),
                    cleanupFailure);
        }
    }

    private TransferHandler handler(TransferKind kind, String key) {
        if (kind == null) throw new CommonException(TransferResultCodeEnum.KIND_IS_REQUIRED);
        TransferHandler handler = handlers.get(kind.name() + ":" + required(key, 64));
        if (handler == null) throw new CommonException(TransferResultCodeEnum.UNKNOWN_TRANSFER_HANDLER);
        return handler;
    }

    private static Map<String, String> options(Map<String, String> options) {
        if (options == null) return Map.of();
        if (options.size() > 30) throw new CommonException(TransferResultCodeEnum.TOO_MANY_TASK_OPTIONS);
        options.forEach((key, value) -> {
            required(key, 64);
            if (!key.matches("[A-Za-z][A-Za-z0-9_.-]*"))
                throw new CommonException(TransferResultCodeEnum.INVALID_OPTION_NAME);
            if (value == null || value.length() > 1000)
                throw new CommonException(TransferResultCodeEnum.INVALID_OPTION_VALUE);
        });
        return Map.copyOf(options);
    }

    private static String tenant(String value) {
        return value == null || value.isBlank() ? DEFAULT_TENANT : required(value, 64);
    }

    private static String required(String value, int max) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new CommonException(CommonResultCodeEnum.PARAM_ILLEGAL);
        }
        return value.trim();
    }

    private static final class BoundedOutputStream extends FilterOutputStream {
        private final long limit;
        private long written;

        private BoundedOutputStream(OutputStream target, long limit) {
            super(target);
            this.limit = limit;
        }

        @Override
        public void write(int value) throws IOException {
            ensureRoom(1);
            out.write(value);
            written++;
        }

        @Override
        public void write(byte[] bytes, int offset, int length) throws IOException {
            ensureRoom(length);
            out.write(bytes, offset, length);
            written += length;
        }

        private void ensureRoom(int length) throws IOException {
            if (length < 0 || length > limit - written) throw new IOException("export output exceeds limit");
        }
    }
}
