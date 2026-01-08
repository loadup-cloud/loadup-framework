package com.github.loadup.upms.api.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Permission Create Command
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
public class PermissionCreateCommand {

  private String parentId;

  @NotBlank(message = "权限名称不能为空")
  @Size(max = 50, message = "权限名称长度不能超过50")
  private String permissionName;

  @NotBlank(message = "权限编码不能为空")
  @Size(max = 100, message = "权限编码长度不能超过100")
  @Pattern(regexp = "^[a-z:]+$", message = "权限编码只能包含小写字母和冒号")
  private String permissionCode;

  /** Permission type: 1-Menu, 2-Button, 3-API */
  private Short permissionType;

  private String resourcePath;

  private String httpMethod;

  private String icon;

  private String componentPath;

  private Integer sortOrder;

  private Boolean visible;

  /** Status: 1-Normal, 0-Disabled */
  private Short status;

  private String remark;

  private String createdBy;
}
