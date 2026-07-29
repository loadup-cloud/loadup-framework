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
