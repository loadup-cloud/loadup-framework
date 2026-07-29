package io.github.loadup.components.dfs.database.dataobject;

/*-
 * #%L
 * Loadup Dfs Binder Database
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;

@Table("file_storage")
public class FileStorageEntity extends BaseDO {
    private String filename;
    private Long fileSize;
    private String contentType;
    private byte[] content;
    private String bizType;
    private String bizId;

    public FileStorageEntity() {}

    public String getFilename() {
        return this.filename;
    }

    public Long getFileSize() {
        return this.fileSize;
    }

    public String getContentType() {
        return this.contentType;
    }

    public byte[] getContent() {
        return this.content;
    }

    public String getBizType() {
        return this.bizType;
    }

    public String getBizId() {
        return this.bizId;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }
}
