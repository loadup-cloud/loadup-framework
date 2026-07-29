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
