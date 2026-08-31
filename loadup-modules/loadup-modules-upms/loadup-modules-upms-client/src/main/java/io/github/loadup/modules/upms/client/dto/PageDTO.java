package io.github.loadup.modules.upms.client.dto;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
