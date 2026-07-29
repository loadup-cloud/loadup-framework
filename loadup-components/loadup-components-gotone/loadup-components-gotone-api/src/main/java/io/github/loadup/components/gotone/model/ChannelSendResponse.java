package io.github.loadup.components.gotone.model;

/*-
 * #%L
 * loadup-components-gotone-api
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 渠道发送响应（内部使用）
 */
public class ChannelSendResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 发送内容
     */
    private String content;

    /**
     * 成功数
     */
    private Integer successCount;

    /**
     * 失败数
     */
    private Integer failedCount;

    /**
     * 每个收件人的成功状态
     */
    private Map<String, Boolean> receiverStatus = new HashMap<>();

    /**
     * 每个收件人的错误信息
     */
    private Map<String, String> receiverErrors = new HashMap<>();

    /**
     * 判断指定收件人是否发送成功
     */
    public boolean isSuccess(String receiver) {
        return Boolean.TRUE.equals(receiverStatus.get(receiver));
    }

    /**
     * 获取指定收件人的错误信息
     */
    public String getErrorMessage(String receiver) {
        return receiverErrors.get(receiver);
    }

    public ChannelSendResponse(String content, Integer successCount, Integer failedCount, Map<String, Boolean> receiverStatus, Map<String, String> receiverErrors) {
        this.content = content;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.receiverStatus = receiverStatus;
        this.receiverErrors = receiverErrors;
    }

    public ChannelSendResponse() {
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public void setReceiverStatus(Map<String, Boolean> receiverStatus) {
        this.receiverStatus = receiverStatus;
    }

    public void setReceiverErrors(Map<String, String> receiverErrors) {
        this.receiverErrors = receiverErrors;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(content, successCount, failedCount, receiverStatus, receiverErrors);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelSendResponse other = (ChannelSendResponse) o;
        if (!java.util.Objects.equals(content, other.content)) return false;
        if (!java.util.Objects.equals(successCount, other.successCount)) return false;
        if (!java.util.Objects.equals(failedCount, other.failedCount)) return false;
        if (!java.util.Objects.equals(receiverStatus, other.receiverStatus)) return false;
        if (!java.util.Objects.equals(receiverErrors, other.receiverErrors)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChannelSendResponse(" + "content=" + content + ", " + "successCount=" + successCount + ", " + "failedCount=" + failedCount + ", " + "receiverStatus=" + receiverStatus + ", " + "receiverErrors=" + receiverErrors + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String content;
        private Integer successCount;
        private Integer failedCount;
        private Map<String, Boolean> receiverStatus = new HashMap<>();
        private Map<String, String> receiverErrors = new HashMap<>();

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder successCount(Integer successCount) {
            this.successCount = successCount;
            return this;
        }

        public Builder failedCount(Integer failedCount) {
            this.failedCount = failedCount;
            return this;
        }

        public Builder receiverStatus(Map<String, Boolean> receiverStatus) {
            this.receiverStatus = receiverStatus;
            return this;
        }

        public Builder receiverErrors(Map<String, String> receiverErrors) {
            this.receiverErrors = receiverErrors;
            return this;
        }

        public ChannelSendResponse build() {
            return new ChannelSendResponse(this.content, this.successCount, this.failedCount, this.receiverStatus, this.receiverErrors);
        }
    }
}
