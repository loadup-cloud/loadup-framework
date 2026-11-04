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
