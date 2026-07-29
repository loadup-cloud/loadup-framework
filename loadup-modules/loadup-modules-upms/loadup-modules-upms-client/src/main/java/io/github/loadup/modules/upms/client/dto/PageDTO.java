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
        PageDTO<T> dto = new PageDTO<>();
        dto.setData(records);
        dto.setTotalCount(total);
        dto.setPageIndex(page);
        dto.setPageSize(size);
        dto.setTotalPages(pages);
        return dto;
    }

    public PageDTO(List<T> data, Long totalCount, Integer pageIndex, Integer pageSize, Integer totalPages) {
        this.data = data;
        this.totalCount = totalCount;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
    }

    public PageDTO() {}

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
}
