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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.InputStream;
import java.util.Map;

/**
 * 文件上传请求
 */
public class FileUploadRequest {

    /**
     * 文件名
     */
    @NotBlank(message = "文件名不能为空")
    private String filename;

    /**
     * 文件输入流
     */
    @NotNull(message = "文件内容不能为空")
    private InputStream inputStream;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 内容类型
     */
    private String contentType;

    /**
     * 业务类型（用于分类存储）
     */
    private String bizType;

    /**
     * 业务ID
     */
    private String bizId;

    /**
     * 存储路径（可选，如不指定则自动生成）
     */
    private String path;

    /**
     * 是否公开访问
     */
    private Boolean publicAccess = false;

    /**
     * 扩展元数据
     */
    private Map<String, String> metadata;

    /**
     * 覆盖已存在的文件
     */
    private Boolean overwrite = false;

    public FileUploadRequest(String filename, InputStream inputStream, Long size, String contentType, String bizType, String bizId, String path, Boolean publicAccess, Map<String, String> metadata, Boolean overwrite) {
        this.filename = filename;
        this.inputStream = inputStream;
        this.size = size;
        this.contentType = contentType;
        this.bizType = bizType;
        this.bizId = bizId;
        this.path = path;
        this.publicAccess = publicAccess;
        this.metadata = metadata;
        this.overwrite = overwrite;
    }

    public FileUploadRequest() {
    }

    public String getFilename() {
        return this.filename;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public Long getSize() {
        return this.size;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getBizType() {
        return this.bizType;
    }

    public String getBizId() {
        return this.bizId;
    }

    public String getPath() {
        return this.path;
    }

    public Boolean isPublicAccess() {
        return this.publicAccess;
    }

    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    public Boolean isOverwrite() {
        return this.overwrite;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setPublicAccess(Boolean publicAccess) {
        this.publicAccess = publicAccess;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public void setOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(filename, inputStream, size, contentType, bizType, bizId, path, publicAccess, metadata, overwrite);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileUploadRequest other = (FileUploadRequest) o;
        if (!java.util.Objects.equals(filename, other.filename)) return false;
        if (!java.util.Objects.equals(inputStream, other.inputStream)) return false;
        if (!java.util.Objects.equals(size, other.size)) return false;
        if (!java.util.Objects.equals(contentType, other.contentType)) return false;
        if (!java.util.Objects.equals(bizType, other.bizType)) return false;
        if (!java.util.Objects.equals(bizId, other.bizId)) return false;
        if (!java.util.Objects.equals(path, other.path)) return false;
        if (!java.util.Objects.equals(publicAccess, other.publicAccess)) return false;
        if (!java.util.Objects.equals(metadata, other.metadata)) return false;
        if (!java.util.Objects.equals(overwrite, other.overwrite)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "FileUploadRequest(" + "filename=" + filename + ", " + "inputStream=" + inputStream + ", " + "size=" + size + ", " + "contentType=" + contentType + ", " + "bizType=" + bizType + ", " + "bizId=" + bizId + ", " + "path=" + path + ", " + "publicAccess=" + publicAccess + ", " + "metadata=" + metadata + ", " + "overwrite=" + overwrite + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String filename;
        private InputStream inputStream;
        private Long size;
        private String contentType;
        private String bizType;
        private String bizId;
        private String path;
        private Boolean publicAccess = false;
        private Map<String, String> metadata;
        private Boolean overwrite = false;

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder inputStream(InputStream inputStream) {
            this.inputStream = inputStream;
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

        public Builder bizType(String bizType) {
            this.bizType = bizType;
            return this;
        }

        public Builder bizId(String bizId) {
            this.bizId = bizId;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
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

        public Builder overwrite(Boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }

        public FileUploadRequest build() {
            return new FileUploadRequest(this.filename, this.inputStream, this.size, this.contentType, this.bizType, this.bizId, this.path, this.publicAccess, this.metadata, this.overwrite);
        }
    }
}
