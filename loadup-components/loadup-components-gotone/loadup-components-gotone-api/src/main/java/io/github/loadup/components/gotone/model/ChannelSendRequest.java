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

    public ChannelSendRequest(
            List<String> receivers,
            String content,
            Map<String, Object> channelConfig,
            Map<String, Object> templateParams) {
        this.receivers = receivers;
        this.content = content;
        this.channelConfig = channelConfig;
        this.templateParams = templateParams;
    }

    public ChannelSendRequest() {}

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

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
