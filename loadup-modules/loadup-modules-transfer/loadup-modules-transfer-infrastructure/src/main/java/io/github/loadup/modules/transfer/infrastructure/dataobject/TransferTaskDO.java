/*
 * #%L
 * LoadUp Transfer Infrastructure
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
package io.github.loadup.modules.transfer.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import io.github.loadup.modules.transfer.domain.model.*;
import java.time.LocalDateTime;

@Table("transfer_task")
public class TransferTaskDO extends BaseDO {
    private String ownerId;
    private TransferKind kind;
    private String handlerKey;
    private String sourceFileId;
    private String resultFileId;
    private TransferStatus status;
    private long processedCount;
    private long totalCount;
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String value) {
        this.ownerId = value;
    }

    public TransferKind getKind() {
        return kind;
    }

    public void setKind(TransferKind value) {
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

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus value) {
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
