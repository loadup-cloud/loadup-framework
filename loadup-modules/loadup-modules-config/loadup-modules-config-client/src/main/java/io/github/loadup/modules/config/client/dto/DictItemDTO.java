package io.github.loadup.modules.config.client.dto;

/*-
 * #%L
 * Loadup Modules Config Client
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
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

/**
 * Data dictionary item DTO.
 *
 * @author LoadUp Framework
 */
public class DictItemDTO {

    private String id;
    private String dictCode;
    private String itemLabel;
    private String itemValue;
    private String parentValue;
    private String cssClass;
    private Integer sortOrder;
    private Boolean enabled;

    public DictItemDTO(
            String id,
            String dictCode,
            String itemLabel,
            String itemValue,
            String parentValue,
            String cssClass,
            Integer sortOrder,
            Boolean enabled) {
        this.id = id;
        this.dictCode = dictCode;
        this.itemLabel = itemLabel;
        this.itemValue = itemValue;
        this.parentValue = parentValue;
        this.cssClass = cssClass;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
    }

    public DictItemDTO() {}

    public String getId() {
        return this.id;
    }

    public String getDictCode() {
        return this.dictCode;
    }

    public String getItemLabel() {
        return this.itemLabel;
    }

    public String getItemValue() {
        return this.itemValue;
    }

    public String getParentValue() {
        return this.parentValue;
    }

    public String getCssClass() {
        return this.cssClass;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    public void setItemValue(String itemValue) {
        this.itemValue = itemValue;
    }

    public void setParentValue(String parentValue) {
        this.parentValue = parentValue;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString(
                this, org.apache.commons.lang3.builder.ToStringStyle.JSON_STYLE);
    }
}
