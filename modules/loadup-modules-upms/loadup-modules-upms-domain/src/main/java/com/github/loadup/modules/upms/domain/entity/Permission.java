package com.github.loadup.modules.upms.domain.entity;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Permission Entity - Resource permission definition
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("upms_permission")
public class Permission {

  @Id private Long id;

  private Long parentId;

  private String permissionName;

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

  private Boolean deleted;

  private String remark;

  private Long createdBy;

  private LocalDateTime createdTime;

  private Long updatedBy;

  private LocalDateTime updatedTime;

  // Transient fields
  @Transient private Permission parent;

  @Transient private List<Permission> children;

  /** Check if permission is enabled */
  public boolean isEnabled() {
    return status != null && status == 1 && !Boolean.TRUE.equals(deleted);
  }

  /** Check if this is a root permission */
  public boolean isRoot() {
    return parentId == null || parentId == 0;
  }

  /** Check if this is a menu permission */
  public boolean isMenu() {
    return permissionType != null && permissionType == 1;
  }

  /** Check if this is a button permission */
  public boolean isButton() {
    return permissionType != null && permissionType == 2;
  }

  /** Check if this is an API permission */
  public boolean isApi() {
    return permissionType != null && permissionType == 3;
  }
}
