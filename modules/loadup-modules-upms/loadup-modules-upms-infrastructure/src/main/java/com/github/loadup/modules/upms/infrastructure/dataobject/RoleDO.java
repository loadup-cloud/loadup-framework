package com.github.loadup.modules.upms.infrastructure.dataobject;

import com.github.loadup.commons.dataobject.BaseDO;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Role Data Object
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("upms_role")
public class RoleDO extends BaseDO {

  private String roleName;

  private String roleCode;

  private String parentRoleId;

  private Integer roleLevel;

  private Short dataScope;

  private Integer sortOrder;

  private Short status;

  private String remark;
}
