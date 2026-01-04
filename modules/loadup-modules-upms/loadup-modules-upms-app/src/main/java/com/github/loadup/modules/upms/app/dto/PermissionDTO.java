package com.github.loadup.modules.upms.app.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Permission DTO (Tree Node)
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

  private String id;
  private String parentId;
  private String permissionName;
  private String permissionCode;
  private Short permissionType;
  private String resourcePath;
  private String httpMethod;
  private String icon;
  private String componentPath;
  private Integer sortOrder;
  private Boolean visible;
  private Short status;
  private List<PermissionDTO> children;
  private String remark;
  private LocalDateTime createdTime;
  private LocalDateTime updatedTime;
}
