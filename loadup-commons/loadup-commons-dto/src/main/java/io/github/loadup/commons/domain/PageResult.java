package io.github.loadup.commons.domain;

import java.util.List;

/** Framework-neutral page result for domain and application contracts. */
public record PageResult<T>(List<T> records, long total, int page, int size) {

    public PageResult {
        records = records == null ? List.of() : List.copyOf(records);
        page = Math.max(page, 1);
        size = Math.max(size, 1);
        total = Math.max(total, 0);
    }

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        return new PageResult<>(records, total, page, size);
    }
}
