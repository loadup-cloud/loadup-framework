package io.github.loadup.modules.upms.infrastructure.security.datascope;

import java.lang.annotation.*;

/**
 * Data Scope Annotation - Controls data access permissions based on user's data scope
 *
 * <p>Data Scope Levels:
 *
 * <ul>
 *   <li>1 - ALL: Access all data
 *   <li>2 - CUSTOM: Custom department selection
 *   <li>3 - DEPT: Current department only
 *   <li>4 - DEPT_AND_SUB: Current department and sub-departments
 *   <li>5 - SELF: Own data only
 * </ul>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

  /**
   * Department table alias (for SQL filtering)
   *
   * @return table alias, default "d"
   */
  String deptAlias() default "d";

  /**
   * User table alias (for SQL filtering when SELF scope)
   *
   * @return table alias, default "u"
   */
  String userAlias() default "u";

  /**
   * Department ID column name
   *
   * @return column name, default "dept_id"
   */
  String deptIdColumn() default "dept_id";

  /**
   * User ID column name (for SELF scope)
   *
   * @return column name, default "user_id"
   */
  String userIdColumn() default "user_id";
}
