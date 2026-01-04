package com.github.loadup.modules.upms.infrastructure.dataobject;

import com.github.loadup.commons.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

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

  @Id private String id;
  private String roleName;

  private String roleCode;

  private String parentRoleId;

  private Integer roleLevel;

  private Short dataScope;

  private Integer sortOrder;

  private Short status;

  private String remark;
}
