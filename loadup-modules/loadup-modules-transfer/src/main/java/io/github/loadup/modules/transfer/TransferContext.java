package io.github.loadup.modules.transfer;

import java.util.Map;

/** Stable task identity, validated options, and progress callback for business handlers. */
public record TransferContext(String taskId, String tenantId, String ownerId, Map<String, String> options,
        ProgressReporter progress) {
    public void report(long processed, long total) { progress.report(processed, total); }

    @FunctionalInterface
    public interface ProgressReporter { void report(long processed, long total); }
}
