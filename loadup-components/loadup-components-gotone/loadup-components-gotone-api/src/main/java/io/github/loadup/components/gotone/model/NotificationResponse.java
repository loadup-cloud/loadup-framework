package io.github.loadup.components.gotone.model;

/*-
 * #%L
 * loadup-components-gotone-api
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

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public Integer getTotalReceivers() {
            return totalReceivers;
        }

        public void setTotalReceivers(Integer totalReceivers) {
            this.totalReceivers = totalReceivers;
        }

        public Integer getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(Integer successCount) {
            this.successCount = successCount;
        }

        public Integer getFailedCount() {
            return failedCount;
        }

        public void setFailedCount(Integer failedCount) {
            this.failedCount = failedCount;
        }

        public Boolean getSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public Integer getTotalReceivers() {
        return totalReceivers;
    }

    public void setTotalReceivers(Integer totalReceivers) {
        this.totalReceivers = totalReceivers;
    }

    public List<ChannelSendResult> getChannelResults() {
        return channelResults;
    }

    public void setChannelResults(List<ChannelSendResult> channelResults) {
        this.channelResults = channelResults;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
