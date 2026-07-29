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
 * 业务代码领域对象
 */
public class BusinessCode extends BaseDomain {
    private String id;
    private String bizCode;
    private String bizName;
    private String description;
    private Boolean enabled;

    public String getId() {
        return this.id;
    }

    public String getBizCode() {
        return this.bizCode;
    }

    public String getBizName() {
        return this.bizName;
    }

    public String getDescription() {
        return this.description;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBizCode(String bizCode) {
        this.bizCode = bizCode;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
