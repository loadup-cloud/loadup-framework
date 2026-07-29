package io.github.loadup.components.gotone.domain;

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
