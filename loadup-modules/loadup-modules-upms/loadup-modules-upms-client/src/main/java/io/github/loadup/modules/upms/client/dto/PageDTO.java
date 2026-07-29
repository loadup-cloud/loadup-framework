package io.github.loadup.modules.upms.client.dto;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
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

import java.util.List;

/**
 * Page Result Wrapper
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
public class PageDTO<T> {

    private List<T> data;
    private Long totalCount;
    private Integer pageIndex;
    private Integer pageSize;
    private Integer totalPages;

    public static <T> PageDTO<T> of(List<T> records, Long total, Integer page, Integer size) {
        int pages = (int) Math.ceil((double) total / size);
        return PageDTO.<T>builder()
                .data(records)
                .totalCount(total)
                .pageIndex(page)
                .pageSize(size)
                .totalPages(pages)
                .build();
    }

    public PageDTO(List<T> data, Long totalCount, Integer pageIndex, Integer pageSize, Integer totalPages) {
        this.data = data;
        this.totalCount = totalCount;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
    }

    public PageDTO() {
    }

    public List<T> getData() {
        return this.data;
    }

    public Long getTotalCount() {
        return this.totalCount;
    }

    public Integer getPageIndex() {
        return this.pageIndex;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public Integer getTotalPages() {
        return this.totalPages;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(data, totalCount, pageIndex, pageSize, totalPages);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageDTO other = (PageDTO) o;
        if (!java.util.Objects.equals(data, other.data)) return false;
        if (!java.util.Objects.equals(totalCount, other.totalCount)) return false;
        if (!java.util.Objects.equals(pageIndex, other.pageIndex)) return false;
        if (!java.util.Objects.equals(pageSize, other.pageSize)) return false;
        if (!java.util.Objects.equals(totalPages, other.totalPages)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "PageDTO(" + "data=" + data + ", " + "totalCount=" + totalCount + ", " + "pageIndex=" + pageIndex + ", " + "pageSize=" + pageSize + ", " + "totalPages=" + totalPages + ")";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<T> data;
        private Long totalCount;
        private Integer pageIndex;
        private Integer pageSize;
        private Integer totalPages;

        public Builder data(List<T> data) {
            this.data = data;
            return this;
        }

        public Builder totalCount(Long totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public Builder pageIndex(Integer pageIndex) {
            this.pageIndex = pageIndex;
            return this;
        }

        public Builder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder totalPages(Integer totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public PageDTO build() {
            return new PageDTO(this.data, this.totalCount, this.pageIndex, this.pageSize, this.totalPages);
        }
    }
}
