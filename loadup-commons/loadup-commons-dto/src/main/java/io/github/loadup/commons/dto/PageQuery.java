package io.github.loadup.commons.dto;

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
