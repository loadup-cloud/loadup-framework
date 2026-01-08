package com.github.loadup.upms.api.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Permission Update Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class PermissionUpdateCommand {

  @NotNull(message = "权限ID不能为空")
  private String id;

  private String parentId;

  @Size(max = 50, message = "权限名称长度不能超过50")
  private String permissionName;

  /** Permission type: 1-Menu, 2-Button, 3-API */
  private Short permissionType;

  private String resourcePath;

  private String httpMethod;

  private String icon;

  private String componentPath;

  private Integer sortOrder;

  private Boolean visible;

  private Short status;

  private String remark;

  private String updatedBy;
}
