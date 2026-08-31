package io.github.loadup.components.gotone.domain;

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

import io.github.loadup.commons.domain.BaseDomain;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知记录领域对象
 */
public class NotificationRecord extends BaseDomain {
    private String id;
    private String traceId;
    private String bizCode;
    private String bizId;
    private String messageId;
    private String channel;
    private List<String> receivers;
    private String templateCode;
    private String title;
    private String content;
    private String provider;
    private String status;
    private Integer retryCount;
    private Integer priority;
    private String errorMessage;
    private LocalDateTime sendTime;

    public String getId() {
        return this.id;
    }

    public String getTraceId() {
        return this.traceId;
    }

    public String getBizCode() {
        return this.bizCode;
    }

    public String getBizId() {
        return this.bizId;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public String getChannel() {
        return this.channel;
    }

    public List<String> getReceivers() {
        return this.receivers;
    }

    public String getTemplateCode() {
        return this.templateCode;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
    }

    public String getProvider() {
        return this.provider;
    }

    public String getStatus() {
        return this.status;
    }

    public Integer getRetryCount() {
        return this.retryCount;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public LocalDateTime getSendTime() {
        return this.sendTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setSendTime(LocalDateTime sendTime) {
        this.sendTime = sendTime;
    }
}
