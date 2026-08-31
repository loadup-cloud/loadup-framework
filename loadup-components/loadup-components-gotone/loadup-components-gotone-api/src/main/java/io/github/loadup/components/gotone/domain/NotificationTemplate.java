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

/**
 * 通知模板领域对象
 */
public class NotificationTemplate extends BaseDomain {
    private String id;
    private String templateCode;
    private String templateName;
    private String channel;
    private String content;
    private String titleTemplate;
    private String templateType;
    private Boolean enabled;

    public String getId() {
        return this.id;
    }

    public String getTemplateCode() {
        return this.templateCode;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public String getChannel() {
        return this.channel;
    }

    public String getContent() {
        return this.content;
    }

    public String getTitleTemplate() {
        return this.titleTemplate;
    }

    public String getTemplateType() {
        return this.templateType;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitleTemplate(String titleTemplate) {
        this.titleTemplate = titleTemplate;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
