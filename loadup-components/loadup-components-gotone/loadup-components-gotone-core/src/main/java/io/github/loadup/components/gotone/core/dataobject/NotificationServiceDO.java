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

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.io.Serializable;

/**
 * 通知服务配置
 */
@Table("gotone_notification_service")
public class NotificationServiceDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.None)
    private String id;

    /**
     * 服务代码（唯一）
     */
    private String serviceCode;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 优先级
     */
    private Integer priority;

    public NotificationServiceDO(String id, String serviceCode, String serviceName, String description, Boolean enabled, Integer priority) {
        this.id = id;
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.description = description;
        this.enabled = enabled;
        this.priority = priority;
    }

    public NotificationServiceDO() {
    }

    public String getId() {
        return this.id;
    }

    public String getServiceCode() {
        return this.serviceCode;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public String getDescription() {
        return this.description;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), id, serviceCode, serviceName, description, enabled, priority);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NotificationServiceDO other = (NotificationServiceDO) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(serviceCode, other.serviceCode)) return false;
        if (!java.util.Objects.equals(serviceName, other.serviceName)) return false;
        if (!java.util.Objects.equals(description, other.description)) return false;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(priority, other.priority)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "NotificationServiceDO(" + "super=" + super.toString() + ", " + "id=" + id + ", " + "serviceCode=" + serviceCode + ", " + "serviceName=" + serviceName + ", " + "description=" + description + ", " + "enabled=" + enabled + ", " + "priority=" + priority + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String serviceCode;
        private String serviceName;
        private String description;
        private Boolean enabled;
        private Integer priority;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder serviceCode(String serviceCode) {
            this.serviceCode = serviceCode;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder priority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public NotificationServiceDO build() {
            return new NotificationServiceDO(this.id, this.serviceCode, this.serviceName, this.description, this.enabled, this.priority);
        }
    }
}
