package com.github.loadup.modules.upms.app.dto;

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
public class PageResult<T> {

  private List<T> records;
  private Long total;
  private Integer page;
  private Integer size;
  private Integer pages;

  public static <T> PageResult<T> of(List<T> records, Long total, Integer page, Integer size) {
    int pages = (int) Math.ceil((double) total / size);
    return PageResult.<T>builder()
        .records(records)
        .total(total)
        .page(page)
        .size(size)
        .pages(pages)
        .build();
  }
}
