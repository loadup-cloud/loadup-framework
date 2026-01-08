package com.github.loadup.modules.upms.infrastructure.dataobject;

import com.github.loadup.commons.dataobject.BaseDO;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Department Data Object
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("upms_department")
public class DepartmentDO extends BaseDO {

  private String parentId;

  private String deptName;

  private String deptCode;

  private Integer deptLevel;

  private Integer sortOrder;

  private String leaderUserId;

  private String mobile;

  private String email;

  private Short status;

  private String remark;
}
