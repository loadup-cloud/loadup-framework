package io.github.loadup.modules.config.client.command;

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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Command to create a dict item.
 */
public class DictItemCreateCommand {

    @NotBlank
    private String dictCode;

    @NotBlank
    @Size(max = 200)
    private String itemLabel;

    @NotBlank
    @Size(max = 200)
    private String itemValue;

    /**
     * Parent value for cascaded dict.
     */
    private String parentValue;

    @Size(max = 100)
    private String cssClass;

    private Integer sortOrder = 0;

    public DictItemCreateCommand(
            String dictCode,
            String itemLabel,
            String itemValue,
            String parentValue,
            String cssClass,
            Integer sortOrder) {
        this.dictCode = dictCode;
        this.itemLabel = itemLabel;
        this.itemValue = itemValue;
        this.parentValue = parentValue;
        this.cssClass = cssClass;
        this.sortOrder = sortOrder;
    }

    public DictItemCreateCommand() {}

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

    @Override
    public int hashCode() {
        return java.util.Objects.hash(dictCode, itemLabel, itemValue, parentValue, cssClass, sortOrder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DictItemCreateCommand other = (DictItemCreateCommand) o;
        if (!java.util.Objects.equals(dictCode, other.dictCode)) return false;
        if (!java.util.Objects.equals(itemLabel, other.itemLabel)) return false;
        if (!java.util.Objects.equals(itemValue, other.itemValue)) return false;
        if (!java.util.Objects.equals(parentValue, other.parentValue)) return false;
        if (!java.util.Objects.equals(cssClass, other.cssClass)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "DictItemCreateCommand(" + "dictCode=" + dictCode + ", " + "itemLabel=" + itemLabel + ", " + "itemValue="
                + itemValue + ", " + "parentValue=" + parentValue + ", " + "cssClass=" + cssClass + ", " + "sortOrder="
                + sortOrder + ")";
    }
}
