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
import java.util.Map;

/**
 * 渠道发送请求（内部使用）
 */
public class ChannelSendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收件人列表
     */
    private List<String> receivers;

    /**
     * 发送内容
     */
    private String content;

    /**
     * 渠道配置（来自 ServiceChannelDO.channelConfig）
     */
    private Map<String, Object> channelConfig;

    /**
     * 模板参数（用于某些渠道的额外处理）
     */
    private Map<String, Object> templateParams;

    public ChannelSendRequest(List<String> receivers, String content, Map<String, Object> channelConfig, Map<String, Object> templateParams) {
        this.receivers = receivers;
        this.content = content;
        this.channelConfig = channelConfig;
        this.templateParams = templateParams;
    }

    public ChannelSendRequest() {
    }

    public List<String> getReceivers() {
        return this.receivers;
    }

    public String getContent() {
        return this.content;
    }

    public Map<String, Object> getChannelConfig() {
        return this.channelConfig;
    }

    public Map<String, Object> getTemplateParams() {
        return this.templateParams;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setChannelConfig(Map<String, Object> channelConfig) {
        this.channelConfig = channelConfig;
    }

    public void setTemplateParams(Map<String, Object> templateParams) {
        this.templateParams = templateParams;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(receivers, content, channelConfig, templateParams);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelSendRequest other = (ChannelSendRequest) o;
        if (!java.util.Objects.equals(receivers, other.receivers)) return false;
        if (!java.util.Objects.equals(content, other.content)) return false;
        if (!java.util.Objects.equals(channelConfig, other.channelConfig)) return false;
        if (!java.util.Objects.equals(templateParams, other.templateParams)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ChannelSendRequest(" + "receivers=" + receivers + ", " + "content=" + content + ", " + "channelConfig=" + channelConfig + ", " + "templateParams=" + templateParams + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> receivers;
        private String content;
        private Map<String, Object> channelConfig;
        private Map<String, Object> templateParams;

        public Builder receivers(List<String> receivers) {
            this.receivers = receivers;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder channelConfig(Map<String, Object> channelConfig) {
            this.channelConfig = channelConfig;
            return this;
        }

        public Builder templateParams(Map<String, Object> templateParams) {
            this.templateParams = templateParams;
            return this;
        }

        public ChannelSendRequest build() {
            return new ChannelSendRequest(this.receivers, this.content, this.channelConfig, this.templateParams);
        }
    }
}
