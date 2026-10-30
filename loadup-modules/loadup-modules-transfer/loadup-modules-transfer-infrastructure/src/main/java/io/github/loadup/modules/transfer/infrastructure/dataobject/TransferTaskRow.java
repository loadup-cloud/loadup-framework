package io.github.loadup.modules.transfer.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

/**
 * Row model for transfer tasks.
 */
@Table("transfer_task")
public class TransferTaskRow extends BaseDO {
    private String ownerId;
    private String kind;
    private String handlerKey;
    private String sourceFileId;
    private String resultFileId;
    private String status;
    private long processedCount;
    private long totalCount;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public TransferTaskRow() {}

    public TransferTaskRow(
            String id,
            String ownerId,
            String kind,
            String handlerKey,
            String sourceFileId,
            String resultFileId,
            String status,
            long processedCount,
            long totalCount,
            String errorMessage,
            LocalDateTime createdAt,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalDateTime updatedAt,
            boolean deleted) {
        setId(id);
        this.ownerId = ownerId;
        this.kind = kind;
        this.handlerKey = handlerKey;
        this.sourceFileId = sourceFileId;
        this.resultFileId = resultFileId;
        this.status = status;
        this.processedCount = processedCount;
        this.totalCount = totalCount;
        this.errorMessage = errorMessage;
        setCreatedAt(createdAt);
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        setUpdatedAt(updatedAt);
        setDeleted(deleted);
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String value) {
        this.ownerId = value;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String value) {
        this.kind = value;
    }

    public String getHandlerKey() {
        return handlerKey;
    }

    public void setHandlerKey(String value) {
        this.handlerKey = value;
    }

    public String getSourceFileId() {
        return sourceFileId;
    }

    public void setSourceFileId(String value) {
        this.sourceFileId = value;
    }

    public String getResultFileId() {
        return resultFileId;
    }

    public void setResultFileId(String value) {
        this.resultFileId = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public long getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(long value) {
        this.processedCount = value;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long value) {
        this.totalCount = value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime value) {
        this.startedAt = value;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime value) {
        this.finishedAt = value;
    }
}
