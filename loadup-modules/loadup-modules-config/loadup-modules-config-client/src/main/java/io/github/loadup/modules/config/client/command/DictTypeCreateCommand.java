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
 * Command to create a dict type.
 */
public class DictTypeCreateCommand {

    @NotBlank
    @Size(max = 100)
    private String dictCode;

    @NotBlank
    @Size(max = 200)
    private String dictName;

    @Size(max = 500)
    private String description;

    private Integer sortOrder = 0;

    public DictTypeCreateCommand(String dictCode, String dictName, String description, Integer sortOrder) {
        this.dictCode = dictCode;
        this.dictName = dictName;
        this.description = description;
        this.sortOrder = sortOrder;
    }

    public DictTypeCreateCommand() {
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

    public Integer getSortOrder() {
        return this.sortOrder;
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

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(dictCode, dictName, description, sortOrder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DictTypeCreateCommand other = (DictTypeCreateCommand) o;
        if (!java.util.Objects.equals(dictCode, other.dictCode)) return false;
        if (!java.util.Objects.equals(dictName, other.dictName)) return false;
        if (!java.util.Objects.equals(description, other.description)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "DictTypeCreateCommand(" + "dictCode=" + dictCode + ", " + "dictName=" + dictName + ", " + "description=" + description + ", " + "sortOrder=" + sortOrder + ")";
    }
}
