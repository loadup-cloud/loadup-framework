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

import java.io.InputStream;

/**
 * 文件下载响应
 */
public class FileDownloadResponse {

    /**
     * 文件元数据
     */
    private FileMetadata metadata;

    /**
     * 文件输入流
     */
    private InputStream inputStream;

    /**
     * 内容长度
     */
    private Long contentLength;

    public FileDownloadResponse(FileMetadata metadata, InputStream inputStream, Long contentLength) {
        this.metadata = metadata;
        this.inputStream = inputStream;
        this.contentLength = contentLength;
    }

    public FileDownloadResponse() {}

    public FileMetadata getMetadata() {
        return this.metadata;
    }

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public Long getContentLength() {
        return this.contentLength;
    }

    public void setMetadata(FileMetadata metadata) {
        this.metadata = metadata;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private FileMetadata metadata;
        private InputStream inputStream;
        private Long contentLength;

        public Builder metadata(FileMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder inputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        public Builder contentLength(Long contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public FileDownloadResponse build() {
            return new FileDownloadResponse(this.metadata, this.inputStream, this.contentLength);
        }
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
