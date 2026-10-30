/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.gotone.store.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

@Table("gotone_notification_record")
public class NotificationRecordRow extends BaseDO {
    private String serviceCode;
    private String traceId;
    private String requestId;
    private String channel;
    private String provider;
    private String receiver;
    private String content;
    private String status;
    private String errorMessage;
    private LocalDateTime sendTime;

    public NotificationRecordRow() {}

    public NotificationRecordRow(
            String id,
            String serviceCode,
            String traceId,
            String requestId,
            String channel,
            String provider,
            String receiver,
            String content,
            String status,
            String errorMessage,
            LocalDateTime sendTime,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean deleted) {
        setId(id);
        this.serviceCode = serviceCode;
        this.traceId = traceId;
        this.requestId = requestId;
        this.channel = channel;
        this.provider = provider;
        this.receiver = receiver;
        this.content = content;
        this.status = status;
        this.errorMessage = errorMessage;
        this.sendTime = sendTime;
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        setDeleted(deleted);
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String value) {
        this.serviceCode = value;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String value) {
        this.traceId = value;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String value) {
        this.requestId = value;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String value) {
        this.channel = value;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String value) {
        this.provider = value;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String value) {
        this.receiver = value;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String value) {
        this.content = value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String value) {
        this.status = value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    public LocalDateTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalDateTime value) {
        this.sendTime = value;
    }
}
