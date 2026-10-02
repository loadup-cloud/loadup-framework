package io.github.loadup.modules.file;

import io.github.loadup.components.dfs.DfsService;
import io.github.loadup.components.dfs.model.FileDownloadResponse;
import io.github.loadup.components.dfs.model.FileMetadata;
import io.github.loadup.components.dfs.model.FileUploadRequest;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

/** File lifecycle and business references; DFS remains responsible for bytes. */
public class FileResourceService {
    private static final String DEFAULT_TENANT = "__default__";
    private final FileResourceRepository repository;
    private final DfsService dfs;

    public FileResourceService(FileResourceRepository repository, DfsService dfs) {
        this.repository = repository;
        this.dfs = dfs;
    }

    public FileResource upload(String tenantId, String ownerId, String filename, String contentType,
            long size, InputStream content) {
        String tenant = tenant(tenantId);
        String owner = required(ownerId, "ownerId", 64);
        String safeName = required(filename, "filename", 255);
        if (safeName.contains("/") || safeName.contains("\\")
                || safeName.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("filename contains invalid characters");
        }
        if (content == null || size < 0) throw new IllegalArgumentException("content and nonnegative size are required");
        String type = contentType == null || contentType.isBlank() ? "application/octet-stream"
                : required(contentType, "contentType", 255);
        FileMetadata stored = dfs.upload(new FileUploadRequest(safeName, content, size, type,
                "resources/" + UUID.randomUUID(), Map.of()));
        LocalDateTime now = LocalDateTime.now();
        FileResource file = new FileResource(UUID.randomUUID().toString(), tenant, owner, stored.fileId(),
                safeName, type, stored.size(), stored.provider(), "ACTIVE", now, now);
        try {
            repository.insert(file);
        } catch (RuntimeException failure) {
            try {
                dfs.delete(stored.fileId());
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
        return file;
    }

    public FileResource get(String tenantId, String id, String actorId, boolean admin) {
        FileResource file = repository.find(tenant(tenantId), required(id, "id", 64))
                .filter(item -> "ACTIVE".equals(item.state()))
                .orElseThrow(() -> new IllegalArgumentException("file not found"));
        authorize(file, actorId, admin);
        return file;
    }

    public FileDownloadResponse download(String tenantId, String id, String actorId, boolean admin) {
        return dfs.download(get(tenantId, id, actorId, admin).storageId());
    }

    public FilePage<FileResource> list(String tenantId, String actorId, boolean admin,
            String ownerId, int page, int size) {
        if (page < 1 || size < 1 || size > 100) throw new IllegalArgumentException("invalid page or size");
        String owner = admin && ownerId != null && !ownerId.isBlank()
                ? required(ownerId, "ownerId", 64) : required(actorId, "actorId", 64);
        return repository.list(tenant(tenantId), owner, page, size);
    }

    @Transactional
    public FileReference attach(String tenantId, String id, String actorId, boolean admin,
            String referenceType, String referenceId) {
        String tenant = tenant(tenantId);
        FileResource file = lockActive(tenant, id, actorId, admin);
        FileReference reference = new FileReference(UUID.randomUUID().toString(), tenant, file.id(),
                key(referenceType, "referenceType"), key(referenceId, "referenceId"), LocalDateTime.now());
        repository.addReference(reference);
        return reference;
    }

    @Transactional
    public void detach(String tenantId, String id, String actorId, boolean admin,
            String referenceType, String referenceId) {
        String tenant = tenant(tenantId);
        lockActive(tenant, id, actorId, admin);
        repository.removeReference(tenant, id, key(referenceType, "referenceType"), key(referenceId, "referenceId"));
    }

    public List<FileReference> references(String tenantId, String id, String actorId, boolean admin) {
        FileResource file = get(tenantId, id, actorId, admin);
        return repository.references(file.tenantId(), file.id());
    }

    /** Commit the pending state before calling DFS; retry cleanupPending after storage failure. */
    @Transactional
    public void requestDeletion(String tenantId, String id, String actorId, boolean admin) {
        String tenant = tenant(tenantId);
        FileResource file = lockActive(tenant, id, actorId, admin);
        if (repository.countReferences(tenant, file.id()) > 0) {
            throw new IllegalStateException("file is referenced by a business object");
        }
        repository.markPending(tenant, file.id());
    }

    /** Idempotent cleanup entry point for operators or a scheduler. */
    public int cleanupPending(int limit) {
        if (limit < 1 || limit > 1000) throw new IllegalArgumentException("limit must be between 1 and 1000");
        int cleaned = 0;
        RuntimeException failure = null;
        for (FileResource file : repository.pending(limit)) {
            try {
                dfs.delete(file.storageId());
                repository.markDeleted(file.tenantId(), file.id());
                cleaned++;
            } catch (RuntimeException ex) {
                if (failure == null) failure = new IllegalStateException("some pending file deletions failed");
                failure.addSuppressed(ex);
            }
        }
        if (failure != null) throw failure;
        return cleaned;
    }

    public void cleanup(String tenantId, String id) {
        FileResource file = repository.find(tenant(tenantId), required(id, "id", 64))
                .filter(item -> "PENDING_DELETE".equals(item.state()))
                .orElseThrow(() -> new IllegalArgumentException("file is not pending deletion"));
        dfs.delete(file.storageId());
        repository.markDeleted(file.tenantId(), file.id());
    }

    private FileResource lockActive(String tenant, String id, String actorId, boolean admin) {
        FileResource file = repository.lock(tenant, required(id, "id", 64))
                .filter(item -> "ACTIVE".equals(item.state()))
                .orElseThrow(() -> new IllegalArgumentException("file not found"));
        authorize(file, actorId, admin);
        return file;
    }

    private static void authorize(FileResource file, String actorId, boolean admin) {
        if (!admin && !file.ownerId().equals(actorId)) throw new IllegalArgumentException("file not found");
    }

    private static String tenant(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? DEFAULT_TENANT : required(tenantId, "tenantId", 64);
    }

    private static String key(String value, String name) {
        String result = required(value, name, 128);
        if (!result.matches("[A-Za-z0-9_.:-]+")) throw new IllegalArgumentException(name + " contains invalid characters");
        return result;
    }

    private static String required(String value, String name, int max) {
        if (value == null || value.isBlank() || value.trim().length() > max) {
            throw new IllegalArgumentException(name + " must contain 1 to " + max + " characters");
        }
        return value.trim();
    }
}
