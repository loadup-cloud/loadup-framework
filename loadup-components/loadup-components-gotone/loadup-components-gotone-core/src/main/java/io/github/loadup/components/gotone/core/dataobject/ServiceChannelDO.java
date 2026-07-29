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
import java.util.List;
import java.util.Map;

/**
 * 服务渠道映射配置
 */
@Table("gotone_service_channel")
public class ServiceChannelDO extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(keyType = KeyType.None)
    private String id;

    /**
     * 服务代码
     */
    private String serviceCode;

    /**
     * 渠道：EMAIL/SMS/PUSH
     */
    private String channel;

    /**
     * 模板代码
     */
    private String templateCode;

    /**
     * 模板内容（支持${var}占位符）
     */
    private String templateContent;

    /**
     * 渠道配置（JSON格式）
     * EMAIL: {"subject": "xxx", "from": "xxx"}
     * SMS: {"signName": "xxx", "templateId": "xxx"}
     * PUSH: {"title": "xxx", "sound": "default"}
     */
    @Column(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> channelConfig;

    /**
     * 主提供商
     */
    private String provider;

    /**
     * 降级提供商列表（JSON格式）
     * ["provider2", "provider3"]
     */
    @Column(typeHandler = JacksonTypeHandler.class)
    private List<String> fallbackProviders;

    /**
     * 发送策略：SYNC/ASYNC/SCHEDULED
     */
    private String sendStrategy;

    /**
     * 重试配置（JSON格式）
     * {"maxRetries": 3, "retryInterval": 60}
     */
    @Column(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> retryConfig;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 优先级（数字越大越优先）
     */
    private Integer priority;

    public ServiceChannelDO(String id, String serviceCode, String channel, String templateCode, String templateContent, Map<String, Object> channelConfig, String provider, List<String> fallbackProviders, String sendStrategy, Map<String, Object> retryConfig, Boolean enabled, Integer priority) {
        this.id = id;
        this.serviceCode = serviceCode;
        this.channel = channel;
        this.templateCode = templateCode;
        this.templateContent = templateContent;
        this.channelConfig = channelConfig;
        this.provider = provider;
        this.fallbackProviders = fallbackProviders;
        this.sendStrategy = sendStrategy;
        this.retryConfig = retryConfig;
        this.enabled = enabled;
        this.priority = priority;
    }

    public ServiceChannelDO() {
    }

    public String getId() {
        return this.id;
    }

    public String getServiceCode() {
        return this.serviceCode;
    }

    public String getChannel() {
        return this.channel;
    }

    public String getTemplateCode() {
        return this.templateCode;
    }

    public String getTemplateContent() {
        return this.templateContent;
    }

    public Map<String, Object> getChannelConfig() {
        return this.channelConfig;
    }

    public String getProvider() {
        return this.provider;
    }

    public List<String> getFallbackProviders() {
        return this.fallbackProviders;
    }

    public String getSendStrategy() {
        return this.sendStrategy;
    }

    public Map<String, Object> getRetryConfig() {
        return this.retryConfig;
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

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    public void setChannelConfig(Map<String, Object> channelConfig) {
        this.channelConfig = channelConfig;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setFallbackProviders(List<String> fallbackProviders) {
        this.fallbackProviders = fallbackProviders;
    }

    public void setSendStrategy(String sendStrategy) {
        this.sendStrategy = sendStrategy;
    }

    public void setRetryConfig(Map<String, Object> retryConfig) {
        this.retryConfig = retryConfig;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), id, serviceCode, channel, templateCode, templateContent, channelConfig, provider, fallbackProviders, sendStrategy, retryConfig, enabled, priority);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ServiceChannelDO other = (ServiceChannelDO) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(serviceCode, other.serviceCode)) return false;
        if (!java.util.Objects.equals(channel, other.channel)) return false;
        if (!java.util.Objects.equals(templateCode, other.templateCode)) return false;
        if (!java.util.Objects.equals(templateContent, other.templateContent)) return false;
        if (!java.util.Objects.equals(channelConfig, other.channelConfig)) return false;
        if (!java.util.Objects.equals(provider, other.provider)) return false;
        if (!java.util.Objects.equals(fallbackProviders, other.fallbackProviders)) return false;
        if (!java.util.Objects.equals(sendStrategy, other.sendStrategy)) return false;
        if (!java.util.Objects.equals(retryConfig, other.retryConfig)) return false;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(priority, other.priority)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ServiceChannelDO(" + "super=" + super.toString() + ", " + "id=" + id + ", " + "serviceCode=" + serviceCode + ", " + "channel=" + channel + ", " + "templateCode=" + templateCode + ", " + "templateContent=" + templateContent + ", " + "channelConfig=" + channelConfig + ", " + "provider=" + provider + ", " + "fallbackProviders=" + fallbackProviders + ", " + "sendStrategy=" + sendStrategy + ", " + "retryConfig=" + retryConfig + ", " + "enabled=" + enabled + ", " + "priority=" + priority + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String serviceCode;
        private String channel;
        private String templateCode;
        private String templateContent;
        private Map<String, Object> channelConfig;
        private String provider;
        private List<String> fallbackProviders;
        private String sendStrategy;
        private Map<String, Object> retryConfig;
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

        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }

        public Builder templateCode(String templateCode) {
            this.templateCode = templateCode;
            return this;
        }

        public Builder templateContent(String templateContent) {
            this.templateContent = templateContent;
            return this;
        }

        public Builder channelConfig(Map<String, Object> channelConfig) {
            this.channelConfig = channelConfig;
            return this;
        }

        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        public Builder fallbackProviders(List<String> fallbackProviders) {
            this.fallbackProviders = fallbackProviders;
            return this;
        }

        public Builder sendStrategy(String sendStrategy) {
            this.sendStrategy = sendStrategy;
            return this;
        }

        public Builder retryConfig(Map<String, Object> retryConfig) {
            this.retryConfig = retryConfig;
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

        public ServiceChannelDO build() {
            return new ServiceChannelDO(this.id, this.serviceCode, this.channel, this.templateCode, this.templateContent, this.channelConfig, this.provider, this.fallbackProviders, this.sendStrategy, this.retryConfig, this.enabled, this.priority);
        }
    }
}
