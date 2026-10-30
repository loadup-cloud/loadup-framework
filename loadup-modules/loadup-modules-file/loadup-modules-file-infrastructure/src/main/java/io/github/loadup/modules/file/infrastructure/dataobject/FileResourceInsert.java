/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.modules.file.infrastructure.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

/**
 * Single file row with an application-assigned identifier, used for insertion and lookup.
 */
@Table("file_resource")
public class FileResourceInsert extends BaseDO {
    private String ownerId;
    private String storageId;
    private String filename;
    private String contentType;
    private long contentLength;
    private String provider;
    private String state;

    public FileResourceInsert() {}

    public FileResourceInsert(
            String id,
            String ownerId,
            String storageId,
            String filename,
            String contentType,
            long contentLength,
            String provider,
            String state,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean deleted) {
        setId(id);
        this.ownerId = ownerId;
        this.storageId = storageId;
        this.filename = filename;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.provider = provider;
        this.state = state;
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeleted(deleted);
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String value) {
        this.ownerId = value;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String value) {
        this.storageId = value;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String value) {
        this.filename = value;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String value) {
        this.contentType = value;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long value) {
        this.contentLength = value;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String value) {
        this.provider = value;
    }

    public String getState() {
        return state;
    }

    public void setState(String value) {
        this.state = value;
    }
}
