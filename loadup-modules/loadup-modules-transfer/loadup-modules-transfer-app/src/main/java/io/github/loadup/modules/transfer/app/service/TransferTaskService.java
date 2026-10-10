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

import io.github.loadup.commons.log.LogUtil;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.modules.file.client.dto.FileResourceDTO;
import io.github.loadup.modules.file.client.facade.FileResourceFacade;
import io.github.loadup.modules.transfer.app.config.TransferTaskProperties;
import io.github.loadup.modules.transfer.app.converter.TransferDTOConverter;
import io.github.loadup.modules.transfer.client.dto.TransferPageDTO;
import io.github.loadup.modules.transfer.client.dto.TransferTaskDTO;
import io.github.loadup.modules.transfer.client.spi.TransferContext;
import io.github.loadup.modules.transfer.client.spi.TransferHandler;
import io.github.loadup.modules.transfer.domain.gateway.TransferGateway;
import io.github.loadup.modules.transfer.domain.model.TransferKind;
import io.github.loadup.modules.transfer.domain.model.TransferStatus;
import io.github.loadup.modules.transfer.domain.model.TransferTask;
import io.github.loadup.retrytask.facade.RetryTaskFacade;
import io.github.loadup.retrytask.facade.RetryTaskProcessor;
import io.github.loadup.retrytask.facade.model.RetryTaskContext;
import io.github.loadup.retrytask.facade.model.RetryTaskRequest;
import io.github.loadup.retrytask.facade.model.RetryTaskStatus;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Persistent task service and RetryTask processor for bounded file transfers. */
public class TransferTaskService
        implements RetryTaskProcessor, io.github.loadup.modules.transfer.client.facade.TransferTaskFacade {

    private static final String BIZ_TYPE = "import-export";
    private static final String REF_TYPE = "transfer-task";
    private static final String DEFAULT_TENANT = "__default__";

    private final TransferDTOConverter converter;
    private final TransferGateway repository;
    private final FileResourceFacade files;
    private final RetryTaskFacade retryTasks;
    private final Map<String, TransferHandler> handlers;
    private final TransferTaskProperties properties;

    public TransferTaskService(
            TransferGateway repository,
            FileResourceFacade files,
            RetryTaskFacade retryTasks,
            List<TransferHandler> handlers,
            TransferTaskProperties properties,
            TransferDTOConverter converter) {
        this.converter = converter;
        this.repository = repository;
        this.files = files;
        this.retryTasks = retryTasks;
        this.properties = properties;
        this.handlers = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        handler -> handler.kind().name() + ":" + handler.key(), Function.identity()));
    }

    public TransferTaskDTO submit(
            String tenantId,
            String ownerId,
            io.github.loadup.modules.transfer.client.enums.TransferKind kind,
            String handlerKey,
            String sourceFileId,
            Map<String, String> options) {
        String tenant = tenant(tenantId);
        String owner = required(ownerId, "ownerId", 64);
        TransferHandler handler = handler(TransferKind.valueOf(kind.name()), handlerKey);
        Map<String, String> args = options(options);
        if (kind == io.github.loadup.modules.transfer.client.enums.TransferKind.IMPORT) {
            FileResourceDTO source = files.get(tenant, required(sourceFileId, "sourceFileId", 64), owner, false);
            if (source.size() > properties.getMaxInputBytes()) {
                throw new IllegalArgumentException("source file exceeds import limit");
            }
        } else if (sourceFileId != null) {
            throw new IllegalArgumentException("export task must not specify sourceFileId");
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
                LocalDateTime.now(),
                null,
                null);
        repository.insert(task, args);
        try {
            protectSource(task);
            enqueue(task.id());
        } catch (RuntimeException failure) {
            repository.fail(task.id(), "Unable to queue task");
            releaseSource(task);
            LogUtil.warn(TransferTaskService.class, "Unable to queue transfer task {}", task.id(), failure);
        }
        return converter.toDTO(repository.find(id).orElseThrow());
    }

    public TransferTaskDTO get(String tenantId, String id, String actorId, boolean admin) {
        return converter.toDTO(requireTask(tenantId, id, actorId, admin));
    }

    private TransferTask requireTask(String tenantId, String id, String actorId, boolean admin) {
        TransferTask task = repository
                .find(required(id, "id", 64))
                .filter(item -> item.tenantId().equals(tenant(tenantId)))
                .orElseThrow(() -> new IllegalArgumentException("task not found"));
        if (!admin && !task.ownerId().equals(actorId)) throw new IllegalArgumentException("task not found");
        return task;
    }

    public TransferPageDTO<TransferTaskDTO> list(
            String tenantId, String actorId, boolean admin, String ownerId, int page, int size) {
        if (page < 1 || size < 1 || size > 100) throw new IllegalArgumentException("invalid page or size");
        String owner = admin && ownerId != null && !ownerId.isBlank()
                ? required(ownerId, "ownerId", 64)
                : required(actorId, "actorId", 64);
        return converter.toPageDTO(repository.list(tenant(tenantId), owner, page, size));
    }

    /** Redispatch a queued task or explicitly replay a failed task. */
    public TransferTaskDTO retry(String tenantId, String id, String actorId, boolean admin) {
        TransferTask task = requireTask(tenantId, id, actorId, admin);
        if (task.status() != TransferStatus.FAILED && task.status() != TransferStatus.QUEUED) {
            throw new IllegalStateException("only queued or failed tasks can be dispatched");
        }
        if (task.status() == TransferStatus.FAILED
                && retryTasks
                        .getStatus(BIZ_TYPE, task.id())
                        .filter(status -> status == RetryTaskStatus.PENDING || status == RetryTaskStatus.PROCESSING)
                        .isPresent()) {
            throw new IllegalStateException("task worker has not finished");
        }
        if (task.sourceFileId() != null) {
            files.get(task.tenantId(), task.sourceFileId(), task.ownerId(), false);
        }
        if (task.status() == TransferStatus.FAILED && !repository.requeue(task.id())) {
            throw new IllegalStateException("task status changed");
        }
        try {
            protectSource(task);
            enqueue(task.id());
        } catch (RuntimeException failure) {
            repository.fail(task.id(), "Unable to queue task");
            releaseSource(task);
            LogUtil.warn(TransferTaskService.class, "Unable to redispatch transfer task {}", task.id(), failure);
        }
        return converter.toDTO(repository.find(task.id()).orElseThrow());
    }

    @Override
    public String bizType() {
        return BIZ_TYPE;
    }

    @Override
    public void process(RetryTaskContext context) throws Exception {
        execute(context.bizId());
    }

    private void execute(String id) throws Exception {
        TransferTask task = repository.find(id).orElseThrow(() -> new IllegalArgumentException("task not found"));
        io.github.loadup.commons.util.TenantUtil.callWithTenant(task.tenantId(), () -> {
            executeForTenant(task);
            return null;
        });
    }

    private void executeForTenant(TransferTask task) throws Exception {
        String id = task.id();
        if (!repository.start(id)) return;
        Path output = null;
        String uploadedId = null;
        try {
            TransferHandler handler = handler(task.kind(), task.handlerKey());
            output = Files.createTempFile("loadup-transfer-", ".tmp");
            TransferContext ctx = new TransferContext(
                    task.id(),
                    task.tenantId(),
                    task.ownerId(),
                    repository.options(id),
                    (processed, total) -> report(id, processed, total));
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
            repository.succeed(id, uploadedId);
            releaseSource(task);
        } catch (Exception failure) {
            if (uploadedId != null) deleteUnlinkedOutput(task, uploadedId);
            try {
                repository.fail(id, "Task processing failed");
            } catch (RuntimeException stateFailure) {
                failure.addSuppressed(stateFailure);
            }
            releaseSource(task);
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

    private void report(String id, long processed, long total) {
        if (processed < 0 || total < processed) throw new IllegalArgumentException("invalid task progress");
        repository.progress(id, processed, total);
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

    private void enqueue(String id) {
        retryTasks.register(new RetryTaskRequest(BIZ_TYPE, id, Map.of(), null, 0));
    }

    private TransferHandler handler(TransferKind kind, String key) {
        if (kind == null) throw new IllegalArgumentException("kind is required");
        TransferHandler handler = handlers.get(kind.name() + ":" + required(key, "handlerKey", 64));
        if (handler == null) throw new IllegalArgumentException("unknown transfer handler");
        return handler;
    }

    private static Map<String, String> options(Map<String, String> options) {
        if (options == null) return Map.of();
        if (options.size() > 30) throw new IllegalArgumentException("too many task options");
        options.forEach((key, value) -> {
            required(key, "option name", 64);
            if (!key.matches("[A-Za-z][A-Za-z0-9_.-]*")) throw new IllegalArgumentException("invalid option name");
            if (value == null || value.length() > 1000) throw new IllegalArgumentException("invalid option value");
        });
        return Map.copyOf(options);
    }

    private static String tenant(String value) {
        return value == null || value.isBlank() ? DEFAULT_TENANT : required(value, "tenantId", 64);
    }

    private static String required(String value, String name, int max) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new IllegalArgumentException(name + " must contain 1 to " + max + " characters");
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
