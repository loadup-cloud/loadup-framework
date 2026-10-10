/*-
 * #%L
 * Loadup Common DTO
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
package io.github.loadup.commons.result;

import io.github.loadup.commons.dto.DTO;
import io.github.loadup.commons.json.ToStringAsJson;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Schema(description = "Application pagination result; use PageResponse for HTTP")
public class PageDTO<T> implements DTO {
    @Schema(description = "Entries on the current page")
    private Collection<T> data;

    @Schema(description = "Pagination metadata")
    private PageInfo pageInfo;

    public static <T> PageDTO<T> of(List<T> records, Long total, Integer page, Integer size) {
        return PageDTO.<T>builder()
                .data(records)
                .pageInfo(new PageInfo(total, size.longValue(), page.longValue()))
                .build();
    }

    /** Maps entries while preserving pagination metadata. */
    public <R> PageDTO<R> map(Function<? super T, ? extends R> mapper) {
        List<R> entries = data.stream().<R>map(mapper).toList();
        return new PageDTO<>(entries, pageInfo);
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
        return ToStringAsJson.reflectionToString(this);
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
