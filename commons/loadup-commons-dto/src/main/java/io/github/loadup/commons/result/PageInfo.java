package io.github.loadup.commons.result;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "分页元数据")
public record PageInfo(
    @Schema(description = "总记录数") Long totalCount,
    @Schema(description = "每页条数") Long pageSize,
    @Schema(description = "当前页码") Long pageIndex) {
  @Schema(description = "总页数")
  public Long getTotalPages() {
    if (pageSize == null || pageSize <= 0) return 0L;
    return (totalCount + pageSize - 1) / pageSize;
  }
}
