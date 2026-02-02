package io.github.loadup.upms.api.dto;

/*-
 * #%L
 * Loadup Modules UPMS Client Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
