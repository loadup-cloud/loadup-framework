/*
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
package io.github.loadup.commons.domain;

import java.util.List;

/**
 * Framework-neutral page result for domain and application contracts.
 */
public record PageResult<T>(List<T> records, long total, int page, int size) {

    public PageResult {
        records = records == null ? List.of() : List.copyOf(records);
        io.github.loadup.commons.dto.PageQuery.of(page, size);
        if (total < 0) {
            throw new io.github.loadup.commons.error.CommonException(
                    io.github.loadup.commons.enums.CommonResultCodeEnum.PARAM_ILLEGAL);
        }
    }

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        return new PageResult<>(records, total, page, size);
    }

    /**
     * Maps entries without changing pagination metadata.
     */
    public <R> PageResult<R> map(java.util.function.Function<? super T, ? extends R> converter) {
        return new PageResult<>(records.stream().<R>map(converter).toList(), total, page, size);
    }

    @Override
    public String toString() {
        return io.github.loadup.commons.json.ToStringAsJson.reflectionToString(this);
    }
}
