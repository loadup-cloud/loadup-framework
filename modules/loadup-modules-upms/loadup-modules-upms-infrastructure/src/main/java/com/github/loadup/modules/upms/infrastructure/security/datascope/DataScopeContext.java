package com.github.loadup.modules.upms.infrastructure.security.datascope;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Scope Context - Holds current user's data scope information
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataScopeContext {

  /** Current user ID */
  private Long userId;

  /** Current user's department ID */
  private Long deptId;

  /** Data scope type (from role) */
  private DataScopeType dataScopeType;

  /** Custom department IDs (for CUSTOM scope) */
  private List<Long> customDeptIds;

  /** Sub-department IDs (for DEPT_AND_SUB scope) */
  private List<Long> subDeptIds;

  /** Whether user is super admin (bypass data scope) */
  private boolean isSuperAdmin;

  /** Generate SQL WHERE clause based on data scope */
  public String generateSqlCondition(
      String deptAlias, String userAlias, String deptIdColumn, String userIdColumn) {
    if (isSuperAdmin || dataScopeType == DataScopeType.ALL) {
      return "1=1"; // No restriction
    }

    switch (dataScopeType) {
      case CUSTOM:
        if (customDeptIds == null || customDeptIds.isEmpty()) {
          return "1=0"; // No access
        }
        return String.format("%s.%s IN (%s)", deptAlias, deptIdColumn, joinIds(customDeptIds));

      case DEPT:
        if (deptId == null) {
          return "1=0"; // No access
        }
        return String.format("%s.%s = %d", deptAlias, deptIdColumn, deptId);

      case DEPT_AND_SUB:
        if (deptId == null) {
          return "1=0"; // No access
        }
        if (subDeptIds == null || subDeptIds.isEmpty()) {
          return String.format("%s.%s = %d", deptAlias, deptIdColumn, deptId);
        }
        return String.format(
            "%s.%s IN (%d,%s)", deptAlias, deptIdColumn, deptId, joinIds(subDeptIds));

      case SELF:
        if (userId == null) {
          return "1=0"; // No access
        }
        return String.format("%s.%s = %d", userAlias, userIdColumn, userId);

      default:
        return "1=0"; // No access by default
    }
  }

  private String joinIds(List<Long> ids) {
    return ids.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
  }
}
