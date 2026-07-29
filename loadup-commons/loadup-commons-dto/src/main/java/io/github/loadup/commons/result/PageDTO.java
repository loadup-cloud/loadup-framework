package io.github.loadup.commons.result;

/*-
 * #%L
 * Loadup Common DTO
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

import io.github.loadup.commons.dto.DTO;
import java.util.Collection;
import java.util.List;

public class PageDTO<T> extends DTO {
    private Collection<T> data;
    private PageInfo pageInfo;

    public static <T> PageDTO<T> of(List<T> records, Long total, Integer page, Integer size) {
        return PageDTO.<T>builder()
                .data(records)
                .pageInfo(new PageInfo(total, size.longValue(), page.longValue()))
                .build();
    }

    public PageDTO(Collection<T> data, PageInfo pageInfo) {
        this.data = data;
        this.pageInfo = pageInfo;
    }

    public PageDTO() {
    }

    public Collection<T> getData() {
        return this.data;
    }

    public PageInfo getPageInfo() {
        return this.pageInfo;
    }

    public void setData(Collection<T> data) {
        this.data = data;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), data, pageInfo);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PageDTO<?> other = (PageDTO<?>) o;
        if (!java.util.Objects.equals(data, other.data)) return false;
        if (!java.util.Objects.equals(pageInfo, other.pageInfo)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "PageDTO(" + "super=" + super.toString() + ", " + "data=" + data + ", " + "pageInfo=" + pageInfo + ")";
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private Collection<T> data;
        private PageInfo pageInfo;

        public Builder<T> data(Collection<T> data) {
            this.data = data;
            return this;
        }

        public Builder<T> pageInfo(PageInfo pageInfo) {
            this.pageInfo = pageInfo;
            return this;
        }

        public PageDTO<T> build() {
            return new PageDTO<>(this.data, this.pageInfo);
        }
    }
}
