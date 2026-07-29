package io.github.loadup.modules.config.infrastructure.dataobject;

/*-
 * #%L
 * Loadup Modules Config Infrastructure
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

import com.mybatisflex.annotation.Table;
import io.github.loadup.commons.dataobject.BaseDO;

@Table("dict_type")
public class DictTypeDO extends BaseDO {

    private String dictCode;
    private String dictName;
    private String description;
    private Boolean systemDefined;
    private Integer sortOrder;
    private Boolean enabled;
    private String createdBy;
    private String updatedBy;

    public DictTypeDO(String dictCode, String dictName, String description, Boolean systemDefined, Integer sortOrder, Boolean enabled, String createdBy, String updatedBy) {
        this.dictCode = dictCode;
        this.dictName = dictName;
        this.description = description;
        this.systemDefined = systemDefined;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public DictTypeDO() {
    }

    public String getDictCode() {
        return this.dictCode;
    }

    public String getDictName() {
        return this.dictName;
    }

    public String getDescription() {
        return this.description;
    }

    public Boolean isSystemDefined() {
        return this.systemDefined;
    }

    public Integer getSortOrder() {
        return this.sortOrder;
    }

    public Boolean isEnabled() {
        return this.enabled;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
    }

    public void setDictCode(String dictCode) {
        this.dictCode = dictCode;
    }

    public void setDictName(String dictName) {
        this.dictName = dictName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSystemDefined(Boolean systemDefined) {
        this.systemDefined = systemDefined;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), dictCode, dictName, description, systemDefined, sortOrder, enabled, createdBy, updatedBy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DictTypeDO other = (DictTypeDO) o;
        if (!java.util.Objects.equals(dictCode, other.dictCode)) return false;
        if (!java.util.Objects.equals(dictName, other.dictName)) return false;
        if (!java.util.Objects.equals(description, other.description)) return false;
        if (!java.util.Objects.equals(systemDefined, other.systemDefined)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(createdBy, other.createdBy)) return false;
        if (!java.util.Objects.equals(updatedBy, other.updatedBy)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "DictTypeDO(" + "super=" + super.toString() + ", " + "dictCode=" + dictCode + ", " + "dictName=" + dictName + ", " + "description=" + description + ", " + "systemDefined=" + systemDefined + ", " + "sortOrder=" + sortOrder + ", " + "enabled=" + enabled + ", " + "createdBy=" + createdBy + ", " + "updatedBy=" + updatedBy + ")";
    }
}
