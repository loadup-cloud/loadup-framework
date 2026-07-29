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

import java.time.LocalDateTime;

/**
 * Data dictionary type DTO.
 *
 * @author LoadUp Framework
 */
public class DictTypeDTO {

    private String id;
    private String dictCode;
    private String dictName;
    private String description;
    private Boolean systemDefined;
    private Integer sortOrder;
    private Boolean enabled;
    private LocalDateTime updatedAt;

    public DictTypeDTO(String id, String dictCode, String dictName, String description, Boolean systemDefined, Integer sortOrder, Boolean enabled, LocalDateTime updatedAt) {
        this.id = id;
        this.dictCode = dictCode;
        this.dictName = dictName;
        this.description = description;
        this.systemDefined = systemDefined;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
        this.updatedAt = updatedAt;
    }

    public DictTypeDTO() {
    }

    public String getId() {
        return this.id;
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

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, dictCode, dictName, description, systemDefined, sortOrder, enabled, updatedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DictTypeDTO other = (DictTypeDTO) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(dictCode, other.dictCode)) return false;
        if (!java.util.Objects.equals(dictName, other.dictName)) return false;
        if (!java.util.Objects.equals(description, other.description)) return false;
        if (!java.util.Objects.equals(systemDefined, other.systemDefined)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(updatedAt, other.updatedAt)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "DictTypeDTO(" + "id=" + id + ", " + "dictCode=" + dictCode + ", " + "dictName=" + dictName + ", " + "description=" + description + ", " + "systemDefined=" + systemDefined + ", " + "sortOrder=" + sortOrder + ", " + "enabled=" + enabled + ", " + "updatedAt=" + updatedAt + ")";
    }
}
