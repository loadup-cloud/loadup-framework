/* Copyright 2026 LoadUp Cloud. Licensed under the Apache License, Version 2.0. */
package io.github.loadup.components.gotone.store.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

@Table("gotone_notification_service")
public class NotificationServiceRow extends BaseDO {
    private String serviceCode;
    private String serviceName;
    private String description;
    private boolean enabled;
    private int priority;

    public NotificationServiceRow() {}

    public NotificationServiceRow(
            String id,
            String serviceCode,
            String serviceName,
            String description,
            boolean enabled,
            int priority,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean deleted) {
        setId(id);
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.description = description;
        this.enabled = enabled;
        this.priority = priority;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String value) {
        this.serviceName = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int value) {
        this.priority = value;
    }
}
