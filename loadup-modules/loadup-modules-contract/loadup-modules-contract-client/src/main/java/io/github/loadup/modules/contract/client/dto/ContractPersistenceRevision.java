package io.github.loadup.modules.contract.client.dto;

/** Frozen revision content and its integrity hash. */
public record ContractPersistenceRevision(int revision, String snapshot, String snapshotHash) {
    @Override
    public String toString() {
        return "ContractPersistenceRevision[revision=" + revision + "]";
    }
}
