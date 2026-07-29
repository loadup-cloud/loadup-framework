package io.github.loadup.modules.config.domain.model;

/*-
 * #%L
 * Loadup Modules Config Domain
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
 * Domain model for a data dictionary item.
 *
 * <p>Pure POJO — no persistence framework annotations.
 */
public class DictItem {

    private String id;
    private String dictCode;
    private String itemLabel;
    private String itemValue;
    /**
     * Null for top-level items; set for cascaded children.
     */
    private String parentValue;

    private String cssClass;
    private Integer sortOrder;
    private Boolean enabled;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public DictItem(
            String id,
            String dictCode,
            String itemLabel,
            String itemValue,
            String parentValue,
            String cssClass,
            Integer sortOrder,
            Boolean enabled,
            String createdBy,
            LocalDateTime createdAt,
            String updatedBy,
            LocalDateTime updatedAt) {
        this.id = id;
        this.dictCode = dictCode;
        this.itemLabel = itemLabel;
        this.itemValue = itemValue;
        this.parentValue = parentValue;
        this.cssClass = cssClass;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public DictItem() {}

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

    public String getCreatedBy() {
        return this.createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public String getUpdatedBy() {
        return this.updatedBy;
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

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(
                id,
                dictCode,
                itemLabel,
                itemValue,
                parentValue,
                cssClass,
                sortOrder,
                enabled,
                createdBy,
                createdAt,
                updatedBy,
                updatedAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DictItem other = (DictItem) o;
        if (!java.util.Objects.equals(id, other.id)) return false;
        if (!java.util.Objects.equals(dictCode, other.dictCode)) return false;
        if (!java.util.Objects.equals(itemLabel, other.itemLabel)) return false;
        if (!java.util.Objects.equals(itemValue, other.itemValue)) return false;
        if (!java.util.Objects.equals(parentValue, other.parentValue)) return false;
        if (!java.util.Objects.equals(cssClass, other.cssClass)) return false;
        if (!java.util.Objects.equals(sortOrder, other.sortOrder)) return false;
        if (!java.util.Objects.equals(enabled, other.enabled)) return false;
        if (!java.util.Objects.equals(createdBy, other.createdBy)) return false;
        if (!java.util.Objects.equals(createdAt, other.createdAt)) return false;
        if (!java.util.Objects.equals(updatedBy, other.updatedBy)) return false;
        if (!java.util.Objects.equals(updatedAt, other.updatedAt)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "DictItem(" + "id=" + id + ", " + "dictCode=" + dictCode + ", " + "itemLabel=" + itemLabel + ", "
                + "itemValue=" + itemValue + ", " + "parentValue=" + parentValue + ", " + "cssClass=" + cssClass + ", "
                + "sortOrder=" + sortOrder + ", " + "enabled=" + enabled + ", " + "createdBy=" + createdBy + ", "
                + "createdAt=" + createdAt + ", " + "updatedBy=" + updatedBy + ", " + "updatedAt=" + updatedAt + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String dictCode;
        private String itemLabel;
        private String itemValue;
        private String parentValue;
        private String cssClass;
        private Integer sortOrder;
        private Boolean enabled;
        private String createdBy;
        private LocalDateTime createdAt;
        private String updatedBy;
        private LocalDateTime updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder dictCode(String dictCode) {
            this.dictCode = dictCode;
            return this;
        }

        public Builder itemLabel(String itemLabel) {
            this.itemLabel = itemLabel;
            return this;
        }

        public Builder itemValue(String itemValue) {
            this.itemValue = itemValue;
            return this;
        }

        public Builder parentValue(String parentValue) {
            this.parentValue = parentValue;
            return this;
        }

        public Builder cssClass(String cssClass) {
            this.cssClass = cssClass;
            return this;
        }

        public Builder sortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public DictItem build() {
            return new DictItem(
                    this.id,
                    this.dictCode,
                    this.itemLabel,
                    this.itemValue,
                    this.parentValue,
                    this.cssClass,
                    this.sortOrder,
                    this.enabled,
                    this.createdBy,
                    this.createdAt,
                    this.updatedBy,
                    this.updatedAt);
        }
    }
}
