/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.dfs.database.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

/**
 * A database DFS object with an application-assigned ID.
 */
@Table("dfs_file")
public class DfsFileInsert extends BaseDO {
    private String filename;
    private long fileSize;
    private String contentType;
    private byte[] content;
    private String metadataJson;

    public DfsFileInsert() {}

    public DfsFileInsert(
            String id,
            String filename,
            long fileSize,
            String contentType,
            byte[] content,
            String metadataJson,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean deleted) {
        setId(id);
        this.filename = filename;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.content = content;
        this.metadataJson = metadataJson;
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeleted(deleted);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String value) {
        this.filename = value;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long value) {
        this.fileSize = value;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String value) {
        this.contentType = value;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] value) {
        this.content = value;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String value) {
        this.metadataJson = value;
    }
}
