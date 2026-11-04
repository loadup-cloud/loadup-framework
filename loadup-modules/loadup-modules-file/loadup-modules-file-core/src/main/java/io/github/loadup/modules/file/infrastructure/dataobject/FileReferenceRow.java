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
