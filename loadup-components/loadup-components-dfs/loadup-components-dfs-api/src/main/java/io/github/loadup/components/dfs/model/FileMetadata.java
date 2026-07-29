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
    private String status;

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

    public FileMetadata() {}

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getPublicAccess() {
        return publicAccess;
    }
}
