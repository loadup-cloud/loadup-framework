package io.github.loadup.components.dfs.database.entity;

/*-
 * #%L
 * loadup-components-dfs-binder-database
 * %%
 * Copyright (C) 2026 LoadUp Cloud
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

import java.time.LocalDateTime;

/**
 * 文件存储实体 - MyBatis-Flex Entity
 */
public class FileStorageEntity {

    private Long id;

    private String fileId;

    private String filename;

    private byte[] content;

    private Long size;

    private String contentType;

    private String hash;

    private String bizType;

    private String bizId;

    private Boolean publicAccess;

    private LocalDateTime uploadTime;

    private String metadata;

    public FileStorageEntity(Long id, String fileId, String filename, byte[] content, Long size, String contentType, String hash, String bizType, String bizId, Boolean publicAccess, LocalDateTime uploadTime, String metadata) {
        this.id = id;
        this.fileId = fileId;
        this.filename = filename;
        this.content = content;
        this.size = size;
        this.contentType = contentType;
        this.hash = hash;
        this.bizType = bizType;
        this.bizId = bizId;
        this.publicAccess = publicAccess;
        this.uploadTime = uploadTime;
        this.metadata = metadata;
    }

    public FileStorageEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public String getFileId() {
        return this.fileId;
    }

    public String getFilename() {
        return this.filename;
    }

    public byte[] getContent() {
        return this.content;
    }

    public Long getSize() {
        return this.size;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getHash() {
        return this.hash;
    }

    public String getBizType() {
        return this.bizType;
    }

    public String getBizId() {
        return this.bizId;
    }

    public Boolean isPublicAccess() {
        return this.publicAccess;
    }

    public LocalDateTime getUploadTime() {
        return this.uploadTime;
    }

    public String getMetadata() {
        return this.metadata;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public void setPublicAccess(Boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, fileId, filename, content, size, contentType, hash, bizType, bizId, publicAccess, uploadTime, metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileStorageEntity other = (FileStorageEntity) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(fileId, other.fileId)) return false;
        if (!java.util.Objects.equals(filename, other.filename)) return false;
        if (!java.util.Objects.equals(content, other.content)) return false;
        if (!java.util.Objects.equals(size, other.size)) return false;
        if (!java.util.Objects.equals(contentType, other.contentType)) return false;
        if (!java.util.Objects.equals(hash, other.hash)) return false;
        if (!java.util.Objects.equals(bizType, other.bizType)) return false;
        if (!java.util.Objects.equals(bizId, other.bizId)) return false;
        if (!java.util.Objects.equals(publicAccess, other.publicAccess)) return false;
        if (!java.util.Objects.equals(uploadTime, other.uploadTime)) return false;
        if (!java.util.Objects.equals(metadata, other.metadata)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "FileStorageEntity(" + "id=" + id + ", " + "fileId=" + fileId + ", " + "filename=" + filename + ", " + "content=" + content + ", " + "size=" + size + ", " + "contentType=" + contentType + ", " + "hash=" + hash + ", " + "bizType=" + bizType + ", " + "bizId=" + bizId + ", " + "publicAccess=" + publicAccess + ", " + "uploadTime=" + uploadTime + ", " + "metadata=" + metadata + ")";
    }
}
