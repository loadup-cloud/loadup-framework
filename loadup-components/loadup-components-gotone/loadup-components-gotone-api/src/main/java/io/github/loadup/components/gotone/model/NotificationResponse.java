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
import java.util.List;

/**
 * 通知发送响应
 */
public class NotificationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 追踪ID（批量发送使用同一个traceId）
     */
    private String traceId;

    /**
     * 服务代码
     */
    private String serviceCode;

    /**
     * 总收件人数
     */
    private Integer totalReceivers;

    /**
     * 各渠道发送结果
     */
    private List<ChannelSendResult> channelResults;

    /**
     * 是否成功（至少一个渠道发送成功）
     */
    private Boolean success;

    /**
     * 错误信息（如果全部失败）
     */
    private String errorMessage;

    /**
     * 渠道发送结果
     */
    public static class ChannelSendResult implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 渠道：EMAIL/SMS/PUSH
         */
        private String channel;

        /**
         * 使用的提供商
         */
        private String provider;

        /**
         * 总收件人数
         */
        private Integer totalReceivers;

        /**
         * 成功数
         */
        private Integer successCount;

        /**
         * 失败数
         */
        private Integer failedCount;

        /**
         * 是否成功
         */
        private Boolean success;

        /**
         * 错误信息
         */
        private String errorMessage;
    }

    public NotificationResponse(String traceId, String serviceCode, Integer totalReceivers, List<ChannelSendResult> channelResults, Boolean success, String errorMessage, String channel, String provider, Integer totalReceivers, Integer successCount, Integer failedCount, Boolean success, String errorMessage) {
        this.traceId = traceId;
        this.serviceCode = serviceCode;
        this.totalReceivers = totalReceivers;
        this.channelResults = channelResults;
        this.success = success;
        this.errorMessage = errorMessage;
        this.channel = channel;
        this.provider = provider;
        this.totalReceivers = totalReceivers;
        this.successCount = successCount;
        this.failedCount = failedCount;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public NotificationResponse() {
    }

    public String getTraceId() {
        return this.traceId;
    }

    public String getServiceCode() {
        return this.serviceCode;
    }

    public Integer getTotalReceivers() {
        return this.totalReceivers;
    }

    public List<ChannelSendResult> getChannelResults() {
        return this.channelResults;
    }

    public Boolean isSuccess() {
        return this.success;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getChannel() {
        return this.channel;
    }

    public String getProvider() {
        return this.provider;
    }

    public Integer getTotalReceivers() {
        return this.totalReceivers;
    }

    public Integer getSuccessCount() {
        return this.successCount;
    }

    public Integer getFailedCount() {
        return this.failedCount;
    }

    public Boolean isSuccess() {
        return this.success;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public void setTotalReceivers(Integer totalReceivers) {
        this.totalReceivers = totalReceivers;
    }

    public void setChannelResults(List<ChannelSendResult> channelResults) {
        this.channelResults = channelResults;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setTotalReceivers(Integer totalReceivers) {
        this.totalReceivers = totalReceivers;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public void setFailedCount(Integer failedCount) {
        this.failedCount = failedCount;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(traceId, serviceCode, totalReceivers, channelResults, success, errorMessage, channel, provider, totalReceivers, successCount, failedCount, success, errorMessage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationResponse other = (NotificationResponse) o;
        if (!java.util.Objects.equals(traceId, other.traceId)) return false;
        if (!java.util.Objects.equals(serviceCode, other.serviceCode)) return false;
        if (!java.util.Objects.equals(totalReceivers, other.totalReceivers)) return false;
        if (!java.util.Objects.equals(channelResults, other.channelResults)) return false;
        if (!java.util.Objects.equals(success, other.success)) return false;
        if (!java.util.Objects.equals(errorMessage, other.errorMessage)) return false;
        if (!java.util.Objects.equals(channel, other.channel)) return false;
        if (!java.util.Objects.equals(provider, other.provider)) return false;
        if (!java.util.Objects.equals(totalReceivers, other.totalReceivers)) return false;
        if (!java.util.Objects.equals(successCount, other.successCount)) return false;
        if (!java.util.Objects.equals(failedCount, other.failedCount)) return false;
        if (!java.util.Objects.equals(success, other.success)) return false;
        if (!java.util.Objects.equals(errorMessage, other.errorMessage)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "NotificationResponse(" + "traceId=" + traceId + ", " + "serviceCode=" + serviceCode + ", " + "totalReceivers=" + totalReceivers + ", " + "channelResults=" + channelResults + ", " + "success=" + success + ", " + "errorMessage=" + errorMessage + ", " + "channel=" + channel + ", " + "provider=" + provider + ", " + "totalReceivers=" + totalReceivers + ", " + "successCount=" + successCount + ", " + "failedCount=" + failedCount + ", " + "success=" + success + ", " + "errorMessage=" + errorMessage + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String traceId;
        private String serviceCode;
        private Integer totalReceivers;
        private List<ChannelSendResult> channelResults;
        private Boolean success;
        private String errorMessage;
        private String channel;
        private String provider;
        private Integer totalReceivers;
        private Integer successCount;
        private Integer failedCount;
        private Boolean success;
        private String errorMessage;

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder serviceCode(String serviceCode) {
            this.serviceCode = serviceCode;
            return this;
        }

        public Builder totalReceivers(Integer totalReceivers) {
            this.totalReceivers = totalReceivers;
            return this;
        }

        public Builder channelResults(List<ChannelSendResult> channelResults) {
            this.channelResults = channelResults;
            return this;
        }

        public Builder success(Boolean success) {
            this.success = success;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder totalReceivers(Integer totalReceivers) {
            this.totalReceivers = totalReceivers;
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

        public Builder success(Boolean success) {
            this.success = success;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public NotificationResponse build() {
            return new NotificationResponse(this.traceId, this.serviceCode, this.totalReceivers, this.channelResults, this.success, this.errorMessage, this.channel, this.provider, this.totalReceivers, this.successCount, this.failedCount, this.success, this.errorMessage);
        }
    }
}
