package io.github.loadup.modules.file;

import java.util.List;
import java.util.Optional;

public interface FileResourceRepository {
    void insert(FileResource file);
    Optional<FileResource> find(String tenantId, String id);
    Optional<FileResource> lock(String tenantId, String id);
    FilePage<FileResource> list(String tenantId, String ownerId, int page, int size);
    void markPending(String tenantId, String id);
    void markDeleted(String tenantId, String id);
    List<FileResource> pending(int limit);
    void addReference(FileReference reference);
    void removeReference(String tenantId, String fileId, String referenceType, String referenceId);
    long countReferences(String tenantId, String fileId);
    List<FileReference> references(String tenantId, String fileId);
}
