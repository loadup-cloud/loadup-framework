package io.github.loadup.components.dfs.model;

/*-
 * #%L
 * loadup-components-dfs-api
 * %%
 * Copyright (C) 2026 LoadUp Cloud
 * %%
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

    public FileUploadRequest(
            String filename,
            InputStream inputStream,
            Long size,
            String contentType,
            String bizType,
            String bizId,
            String path,
            Boolean publicAccess,
            Map<String, String> metadata,
            Boolean overwrite) {
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

    public FileUploadRequest() {}

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
            return new FileUploadRequest(
                    this.filename,
                    this.inputStream,
                    this.size,
                    this.contentType,
                    this.bizType,
                    this.bizId,
                    this.path,
                    this.publicAccess,
                    this.metadata,
                    this.overwrite);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
