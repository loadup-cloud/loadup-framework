package io.github.loadup.components.dfs.model;

/*-
 * #%L
 * loadup-components-dfs-api
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

import io.github.loadup.components.dfs.enums.FileStatus;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 文件元数据
 */
public class FileMetadata {

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 存储提供者
     */
    private String provider;

    /**
     * 存储路径
     */
    private String path;

    /**
     * 访问URL
     */
    private String url;

    /**
     * 文件哈希值（MD5/SHA256）
     */
    private String hash;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务ID
     */
    private String bizId;

    /**
     * 文件状态
     */
    private FileStatus status;

    /**
     * 是否公开访问
     */
    private Boolean publicAccess;

    /**
     * 扩展元数据
     */
    private Map<String, String> metadata;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 上传者
     */
    private String uploader;

    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessTime;

    /**
     * 访问次数
     */
    private Long accessCount;

    public FileMetadata(String fileId, String filename, Long size, String contentType, String provider, String path, String url, String hash, String bizType, String bizId, FileStatus status, Boolean publicAccess, Map<String, String> metadata, LocalDateTime uploadTime, String uploader, LocalDateTime lastAccessTime, Long accessCount) {
        this.fileId = fileId;
        this.filename = filename;
        this.size = size;
        this.contentType = contentType;
        this.provider = provider;
        this.path = path;
        this.url = url;
        this.hash = hash;
        this.bizType = bizType;
        this.bizId = bizId;
        this.status = status;
        this.publicAccess = publicAccess;
        this.metadata = metadata;
        this.uploadTime = uploadTime;
        this.uploader = uploader;
        this.lastAccessTime = lastAccessTime;
        this.accessCount = accessCount;
    }

    public FileMetadata() {
    }

    public String getFileId() {
        return this.fileId;
    }

    public String getFilename() {
        return this.filename;
    }

    public Long getSize() {
        return this.size;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getProvider() {
        return this.provider;
    }

    public String getPath() {
        return this.path;
    }

    public String getUrl() {
        return this.url;
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

    public FileStatus getStatus() {
        return this.status;
    }

    public Boolean isPublicAccess() {
        return this.publicAccess;
    }

    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    public LocalDateTime getUploadTime() {
        return this.uploadTime;
    }

    public String getUploader() {
        return this.uploader;
    }

    public LocalDateTime getLastAccessTime() {
        return this.lastAccessTime;
    }

    public Long getAccessCount() {
        return this.accessCount;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public void setStatus(FileStatus status) {
        this.status = status;
    }

    public void setPublicAccess(Boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public void setUploader(String uploader) {
        this.uploader = uploader;
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(fileId, filename, size, contentType, provider, path, url, hash, bizType, bizId, status, publicAccess, metadata, uploadTime, uploader, lastAccessTime, accessCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMetadata other = (FileMetadata) o;
        if (!java.util.Objects.equals(fileId, other.fileId)) return false;
        if (!java.util.Objects.equals(filename, other.filename)) return false;
        if (!java.util.Objects.equals(size, other.size)) return false;
        if (!java.util.Objects.equals(contentType, other.contentType)) return false;
        if (!java.util.Objects.equals(provider, other.provider)) return false;
        if (!java.util.Objects.equals(path, other.path)) return false;
        if (!java.util.Objects.equals(url, other.url)) return false;
        if (!java.util.Objects.equals(hash, other.hash)) return false;
        if (!java.util.Objects.equals(bizType, other.bizType)) return false;
        if (!java.util.Objects.equals(bizId, other.bizId)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(publicAccess, other.publicAccess)) return false;
        if (!java.util.Objects.equals(metadata, other.metadata)) return false;
        if (!java.util.Objects.equals(uploadTime, other.uploadTime)) return false;
        if (!java.util.Objects.equals(uploader, other.uploader)) return false;
        if (!java.util.Objects.equals(lastAccessTime, other.lastAccessTime)) return false;
        if (!java.util.Objects.equals(accessCount, other.accessCount)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "FileMetadata(" + "fileId=" + fileId + ", " + "filename=" + filename + ", " + "size=" + size + ", " + "contentType=" + contentType + ", " + "provider=" + provider + ", " + "path=" + path + ", " + "url=" + url + ", " + "hash=" + hash + ", " + "bizType=" + bizType + ", " + "bizId=" + bizId + ", " + "status=" + status + ", " + "publicAccess=" + publicAccess + ", " + "metadata=" + metadata + ", " + "uploadTime=" + uploadTime + ", " + "uploader=" + uploader + ", " + "lastAccessTime=" + lastAccessTime + ", " + "accessCount=" + accessCount + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fileId;
        private String filename;
        private Long size;
        private String contentType;
        private String provider;
        private String path;
        private String url;
        private String hash;
        private String bizType;
        private String bizId;
        private FileStatus status;
        private Boolean publicAccess;
        private Map<String, String> metadata;
        private LocalDateTime uploadTime;
        private String uploader;
        private LocalDateTime lastAccessTime;
        private Long accessCount;

        public Builder fileId(String fileId) {
            this.fileId = fileId;
            return this;
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder size(Long size) {
            this.size = size;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder hash(String hash) {
            this.hash = hash;
            return this;
        }

        public Builder bizType(String bizType) {
            this.bizType = bizType;
            return this;
        }

        public Builder bizId(String bizId) {
            this.bizId = bizId;
            return this;
        }

        public Builder status(FileStatus status) {
            this.status = status;
            return this;
        }

        public Builder publicAccess(Boolean publicAccess) {
            this.publicAccess = publicAccess;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder uploadTime(LocalDateTime uploadTime) {
            this.uploadTime = uploadTime;
            return this;
        }

        public Builder uploader(String uploader) {
            this.uploader = uploader;
            return this;
        }

        public Builder lastAccessTime(LocalDateTime lastAccessTime) {
            this.lastAccessTime = lastAccessTime;
            return this;
        }

        public Builder accessCount(Long accessCount) {
            this.accessCount = accessCount;
            return this;
        }

        public FileMetadata build() {
            return new FileMetadata(this.fileId, this.filename, this.size, this.contentType, this.provider, this.path, this.url, this.hash, this.bizType, this.bizId, this.status, this.publicAccess, this.metadata, this.uploadTime, this.uploader, this.lastAccessTime, this.accessCount);
        }
    }
}
