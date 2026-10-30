/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.modules.file.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

/**
 * A reference row; parent-file lifecycle is managed explicitly by the gateway.
 */
@Table("file_resource_reference")
public class FileReferenceRow extends BaseDO {
    private String fileId;
    private String referenceType;
    private String referenceId;

    public FileReferenceRow() {}

    public FileReferenceRow(
            String id,
            String fileId,
            String referenceType,
            String referenceId,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean deleted) {
        setId(id);
        this.fileId = fileId;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeleted(deleted);
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String value) {
        this.fileId = value;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String value) {
        this.referenceType = value;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String value) {
        this.referenceId = value;
    }
}
