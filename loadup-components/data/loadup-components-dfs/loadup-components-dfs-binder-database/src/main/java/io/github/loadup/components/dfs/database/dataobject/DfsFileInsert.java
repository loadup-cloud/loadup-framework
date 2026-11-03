/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
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
