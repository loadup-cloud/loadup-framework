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
