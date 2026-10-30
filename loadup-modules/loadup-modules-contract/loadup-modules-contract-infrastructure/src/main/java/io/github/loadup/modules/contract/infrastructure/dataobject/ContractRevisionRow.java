package io.github.loadup.modules.contract.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

/**
 * Row model for immutable contract revisions.
 */
@Table("merchant_contract_revision")
public class ContractRevisionRow extends BaseDO {
    private String contractId;
    private int revision;
    private String snapshot;
    private String snapshotHash;

    public ContractRevisionRow() {}

    public ContractRevisionRow(
            String id,
            String contractId,
            int revision,
            String snapshot,
            String snapshotHash,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean deleted) {
        setId(id);
        this.contractId = contractId;
        this.revision = revision;
        this.snapshot = snapshot;
        this.snapshotHash = snapshotHash;
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeleted(deleted);
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String value) {
        this.contractId = value;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int value) {
        this.revision = value;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String value) {
        this.snapshot = value;
    }

    public String getSnapshotHash() {
        return snapshotHash;
    }

    public void setSnapshotHash(String value) {
        this.snapshotHash = value;
    }
}
