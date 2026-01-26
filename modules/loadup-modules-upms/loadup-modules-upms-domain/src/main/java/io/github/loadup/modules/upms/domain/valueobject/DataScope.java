package io.github.loadup.modules.upms.domain.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Scope Value Object Represents different levels of data access scope
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataScope {

  public static final short ALL = 1;
  public static final short CUSTOM = 2;
  public static final short DEPT = 3;
  public static final short DEPT_AND_CHILDREN = 4;
  public static final short SELF_ONLY = 5;

  private short scope;

  private String description;

  public static DataScope all() {
    return new DataScope(ALL, "全部数据权限");
  }

  public static DataScope custom() {
    return new DataScope(CUSTOM, "自定义数据权限");
  }

  public static DataScope dept() {
    return new DataScope(DEPT, "本部门数据权限");
  }

  public static DataScope deptAndChildren() {
    return new DataScope(DEPT_AND_CHILDREN, "本部门及子部门数据权限");
  }

  public static DataScope selfOnly() {
    return new DataScope(SELF_ONLY, "仅本人数据权限");
  }

  public static DataScope of(short scope) {
    return switch (scope) {
      case ALL -> all();
      case CUSTOM -> custom();
      case DEPT -> dept();
      case DEPT_AND_CHILDREN -> deptAndChildren();
      case SELF_ONLY -> selfOnly();
      default -> throw new IllegalArgumentException("Invalid data scope: " + scope);
    };
  }

  public boolean isAll() {
    return scope == ALL;
  }

  public boolean isCustom() {
    return scope == CUSTOM;
  }

  public boolean isDept() {
    return scope == DEPT;
  }

  public boolean isDeptAndChildren() {
    return scope == DEPT_AND_CHILDREN;
  }

  public boolean isSelfOnly() {
    return scope == SELF_ONLY;
  }
}
