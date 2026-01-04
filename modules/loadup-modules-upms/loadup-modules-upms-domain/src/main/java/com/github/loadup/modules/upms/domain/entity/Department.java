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
 * Department Entity - Organizational structure with unlimited hierarchy
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("upms_department")
public class Department {

  @Id private String id;

  private String parentId;

  private String deptName;

  private String deptCode;

  private Integer deptLevel;

  private Integer sortOrder;

  private String leaderUserId;

  private String phone;

  private String email;

  /** Status: 1-Normal, 0-Disabled */
  private Short status;

  private Boolean deleted;

  private String remark;

  private String createdBy;

  private LocalDateTime createdTime;

  private String updatedBy;

  private LocalDateTime updatedTime;

  // Transient fields
  @Transient private Department parent;

  @Transient private List<Department> children;

  @Transient private User leader;

  /** Check if department is enabled */
  public boolean isEnabled() {
    return status != null && status == 1 && !Boolean.TRUE.equals(deleted);
  }

  /** Check if this is a root department */
  public boolean isRoot() {
    return parentId == null || "0".equals(parentId);
  }

  /** Get full path (for display in tree structure) */
  public String getFullPath() {
    if (parent == null || isRoot()) {
      return deptName;
    }
    return parent.getFullPath() + " / " + deptName;
  }
}
