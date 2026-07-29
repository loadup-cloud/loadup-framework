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
import java.util.List;

/**
 * 渠道映射领域对象
 */
public class ChannelMapping extends BaseDomain {
    private String id;
    private String businessCode;
    private String channel;
    private String templateCode;
    private List<String> providerList;
    private Integer priority;
    private Boolean enabled;

    public String getId() {
        return this.id;
    }

    public String getBusinessCode() {
        return this.businessCode;
    }

    public String getChannel() {
        return this.channel;
    }

    public String getTemplateCode() {
        return this.templateCode;
    }

    public List<String> getProviderList() {
        return this.providerList;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public void setProviderList(List<String> providerList) {
        this.providerList = providerList;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
