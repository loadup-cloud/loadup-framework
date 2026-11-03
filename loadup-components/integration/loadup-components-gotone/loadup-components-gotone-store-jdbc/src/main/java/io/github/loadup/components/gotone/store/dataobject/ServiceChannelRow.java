/*
 * Copyright (C) 2025-2026 LoadUp Cloud
 *
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
 */
package io.github.loadup.components.gotone.store.dataobject;

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;
import java.time.LocalDateTime;

@Table("gotone_service_channel")
public class ServiceChannelRow extends BaseDO {
    private String serviceCode;
    private String channel;
    private String templateContent;
    private String channelConfig;
    private String provider;
    private String fallbackProviders;
    private boolean enabled;
    private int priority;

    public ServiceChannelRow() {}

    public ServiceChannelRow(
            String id,
            String serviceCode,
            String channel,
            String templateContent,
            String channelConfig,
            String provider,
            String fallbackProviders,
            boolean enabled,
            int priority,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean deleted) {
        setId(id);
        this.serviceCode = serviceCode;
        this.channel = channel;
        this.templateContent = templateContent;
        this.channelConfig = channelConfig;
        this.provider = provider;
        this.fallbackProviders = fallbackProviders;
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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String value) {
        this.channel = value;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(String value) {
        this.templateContent = value;
    }

    public String getChannelConfig() {
        return channelConfig;
    }

    public void setChannelConfig(String value) {
        this.channelConfig = value;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String value) {
        this.provider = value;
    }

    public String getFallbackProviders() {
        return fallbackProviders;
    }

    public void setFallbackProviders(String value) {
        this.fallbackProviders = value;
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
