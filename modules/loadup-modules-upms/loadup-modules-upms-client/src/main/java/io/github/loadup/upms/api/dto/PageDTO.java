package io.github.loadup.upms.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Page Result Wrapper
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
