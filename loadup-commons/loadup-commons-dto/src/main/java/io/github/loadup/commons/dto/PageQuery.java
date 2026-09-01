package io.github.loadup.commons.dto;

/*-
 * #%L
 * LoadUp Commons DTO
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

/**
 * Framework-neutral pagination query parameters.
 *
 * <p>Used by domain gateway APIs instead of Spring Data {@code Pageable} so the domain layer
 * stays free of framework dependencies. Page numbers are 1-based.
 *
 * @param pageNum 1-based page number, defaults to 1
 * @param pageSize page size, defaults to 20
 */
public record PageQuery(int pageNum, int pageSize) {

    public static final int DEFAULT_PAGE_SIZE = 20;

    public PageQuery {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
    }

    public static PageQuery of(int pageNum, int pageSize) {
        return new PageQuery(pageNum, pageSize);
    }
}
