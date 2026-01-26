package io.github.loadup.modules.upms.infrastructure.dataobject;

import io.github.loadup.commons.dataobject.BaseDO;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
