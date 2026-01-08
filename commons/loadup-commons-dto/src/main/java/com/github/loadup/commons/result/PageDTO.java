package com.github.loadup.commons.result;

import com.github.loadup.commons.dto.DTO;
import java.util.Collection;
import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class PageDTO<T> extends DTO {
  private Collection<T> data;
  private PageInfo pageInfo;

  public static <T> PageDTO<T> of(List<T> records, Long total, Integer page, Integer size) {
    return PageDTO.<T>builder()
        .data(records)
        .pageInfo(new PageInfo(total, size.longValue(), page.longValue()))
        .build();
  }
}
