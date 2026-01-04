package com.github.loadup.modules.upms.infrastructure.dataobject;

import com.github.loadup.commons.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Permission Data Object
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("upms_permission")
public class PermissionDO extends BaseDO {

  @Id private String id;
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

  private String remark;
}
