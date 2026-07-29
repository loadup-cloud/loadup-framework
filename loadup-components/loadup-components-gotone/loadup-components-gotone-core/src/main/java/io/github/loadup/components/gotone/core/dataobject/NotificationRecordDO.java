package io.github.loadup.components.gotone.core.dataobject;

/*-
 * #%L
 * loadup-components-gotone-core
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

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import io.github.loadup.commons.dataobject.BaseDO;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知发送记录 - 单表+JSON扩展字段架构
 */
@Table("gotone_notification_record")
public class NotificationRecordDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.None)
    private String id;

    /**
     * 服务代码（业务标识）
     */
    private String serviceCode;

    /**
     * 追踪ID - 批量发送时相同
     */
    private String traceId;

    /**
     * 请求ID - 用于幂等性
     */
    private String requestId;

    /**
     * 渠道：EMAIL/SMS/PUSH
     */
    private String channel;

    /**
     * 实际使用的提供商
     */
    private String provider;

    /**
     * 收件人（邮箱/手机号/设备Token）
     */
    private String receiver;

    /**
     * 模板代码
     */
    private String templateCode;

    /**
     * 实际发送内容
     */
    private String content;

    /**
     * 渠道扩展数据（JSON格式）
     * EMAIL: {"subject": "xxx", "cc": [], "bcc": [], "attachments": []}
     * SMS: {"phoneNumber": "xxx", "signName": "xxx", "templateId": "xxx"}
     * PUSH: {"title": "xxx", "badge": 1, "sound": "default", "extras": {}}
     */
    @Column(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> channelData;

    /**
     * 状态：PENDING/SUCCESS/FAILED/RETRY
     */
    private String status;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 成功时间
     */
    private LocalDateTime successTime;

    public NotificationRecordDO(String id, String serviceCode, String traceId, String requestId, String channel, String provider, String receiver, String templateCode, String content, Map<String, Object> channelData, String status, String errorCode, String errorMessage, Integer retryCount, Integer maxRetries, LocalDateTime nextRetryTime, LocalDateTime sendTime, LocalDateTime successTime) {
        this.id = id;
        this.serviceCode = serviceCode;
        this.traceId = traceId;
        this.requestId = requestId;
        this.channel = channel;
        this.provider = provider;
        this.receiver = receiver;
        this.templateCode = templateCode;
        this.content = content;
        this.channelData = channelData;
        this.status = status;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.retryCount = retryCount;
        this.maxRetries = maxRetries;
        this.nextRetryTime = nextRetryTime;
        this.sendTime = sendTime;
        this.successTime = successTime;
    }

    public NotificationRecordDO() {
    }

    public String getId() {
        return this.id;
    }

    public String getServiceCode() {
        return this.serviceCode;
    }

    public String getTraceId() {
        return this.traceId;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public String getChannel() {
        return this.channel;
    }

    public String getProvider() {
        return this.provider;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public String getTemplateCode() {
        return this.templateCode;
    }

    public String getContent() {
        return this.content;
    }

    public Map<String, Object> getChannelData() {
        return this.channelData;
    }

    public String getStatus() {
        return this.status;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public Integer getRetryCount() {
        return this.retryCount;
    }

    public Integer getMaxRetries() {
        return this.maxRetries;
    }

    public LocalDateTime getNextRetryTime() {
        return this.nextRetryTime;
    }

    public LocalDateTime getSendTime() {
        return this.sendTime;
    }

    public LocalDateTime getSuccessTime() {
        return this.successTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setChannelData(Map<String, Object> channelData) {
        this.channelData = channelData;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void setNextRetryTime(LocalDateTime nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }

    public void setSuccessTime(LocalDateTime successTime) {
        this.successTime = successTime;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), id, serviceCode, traceId, requestId, channel, provider, receiver, templateCode, content, channelData, status, errorCode, errorMessage, retryCount, maxRetries, nextRetryTime, sendTime, successTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NotificationRecordDO other = (NotificationRecordDO) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(serviceCode, other.serviceCode)) return false;
        if (!java.util.Objects.equals(traceId, other.traceId)) return false;
        if (!java.util.Objects.equals(requestId, other.requestId)) return false;
        if (!java.util.Objects.equals(channel, other.channel)) return false;
        if (!java.util.Objects.equals(provider, other.provider)) return false;
        if (!java.util.Objects.equals(receiver, other.receiver)) return false;
        if (!java.util.Objects.equals(templateCode, other.templateCode)) return false;
        if (!java.util.Objects.equals(content, other.content)) return false;
        if (!java.util.Objects.equals(channelData, other.channelData)) return false;
        if (!java.util.Objects.equals(status, other.status)) return false;
        if (!java.util.Objects.equals(errorCode, other.errorCode)) return false;
        if (!java.util.Objects.equals(errorMessage, other.errorMessage)) return false;
        if (!java.util.Objects.equals(retryCount, other.retryCount)) return false;
        if (!java.util.Objects.equals(maxRetries, other.maxRetries)) return false;
        if (!java.util.Objects.equals(nextRetryTime, other.nextRetryTime)) return false;
        if (!java.util.Objects.equals(sendTime, other.sendTime)) return false;
        if (!java.util.Objects.equals(successTime, other.successTime)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "NotificationRecordDO(" + "super=" + super.toString() + ", " + "id=" + id + ", " + "serviceCode=" + serviceCode + ", " + "traceId=" + traceId + ", " + "requestId=" + requestId + ", " + "channel=" + channel + ", " + "provider=" + provider + ", " + "receiver=" + receiver + ", " + "templateCode=" + templateCode + ", " + "content=" + content + ", " + "channelData=" + channelData + ", " + "status=" + status + ", " + "errorCode=" + errorCode + ", " + "errorMessage=" + errorMessage + ", " + "retryCount=" + retryCount + ", " + "maxRetries=" + maxRetries + ", " + "nextRetryTime=" + nextRetryTime + ", " + "sendTime=" + sendTime + ", " + "successTime=" + successTime + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String serviceCode;
        private String traceId;
        private String requestId;
        private String channel;
        private String provider;
        private String receiver;
        private String templateCode;
        private String content;
        private Map<String, Object> channelData;
        private String status;
        private String errorCode;
        private String errorMessage;
        private Integer retryCount;
        private Integer maxRetries;
        private LocalDateTime nextRetryTime;
        private LocalDateTime sendTime;
        private LocalDateTime successTime;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder serviceCode(String serviceCode) {
            this.serviceCode = serviceCode;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
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

        public Builder receiver(String receiver) {
            this.receiver = receiver;
            return this;
        }

        public Builder templateCode(String templateCode) {
            this.templateCode = templateCode;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder channelData(Map<String, Object> channelData) {
            this.channelData = channelData;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder retryCount(Integer retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public Builder maxRetries(Integer maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder nextRetryTime(LocalDateTime nextRetryTime) {
            this.nextRetryTime = nextRetryTime;
            return this;
        }

        public Builder sendTime(LocalDateTime sendTime) {
            this.sendTime = sendTime;
            return this;
        }

        public Builder successTime(LocalDateTime successTime) {
            this.successTime = successTime;
            return this;
        }

        public NotificationRecordDO build() {
            return new NotificationRecordDO(this.id, this.serviceCode, this.traceId, this.requestId, this.channel, this.provider, this.receiver, this.templateCode, this.content, this.channelData, this.status, this.errorCode, this.errorMessage, this.retryCount, this.maxRetries, this.nextRetryTime, this.sendTime, this.successTime);
        }
    }
}
