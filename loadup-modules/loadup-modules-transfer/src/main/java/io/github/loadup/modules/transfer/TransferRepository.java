package io.github.loadup.modules.transfer;

import java.util.Map;
import java.util.Optional;

public interface TransferRepository {
    void insert(TransferTask task, Map<String, String> options);
    Optional<TransferTask> find(String id);
    TransferPage<TransferTask> list(String tenantId, String ownerId, int page, int size);
    Map<String, String> options(String id);
    boolean start(String id);
    void progress(String id, long processed, long total);
    void succeed(String id, String resultFileId);
    void fail(String id, String message);
    boolean requeue(String id);
}
