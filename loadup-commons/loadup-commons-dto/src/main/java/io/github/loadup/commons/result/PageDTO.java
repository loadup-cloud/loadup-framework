package io.github.loadup.commons.result;

import io.github.loadup.commons.dto.DTO;
import java.util.Collection;
import java.util.List;

public class PageDTO<T> implements DTO {
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

    public PageDTO() {}

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
    public String toString() {
        return toJsonString();
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
