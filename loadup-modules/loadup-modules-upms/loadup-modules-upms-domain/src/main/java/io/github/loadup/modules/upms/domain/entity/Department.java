package io.github.loadup.modules.upms.domain.entity;

/*-
 * #%L
 * Loadup Modules UPMS Domain Layer
 * %%
 * Copyright (C) 2025 - 2026 LoadUp Cloud
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class Department {

  private String id;

  private String parentId;

  private String deptName;

  private String deptCode;

  private Integer deptLevel;

  private Integer sortOrder;

  private String leaderUserId;

  private String mobile;

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
  private Department parent;

  private List<Department> children;

  private User leader;

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
