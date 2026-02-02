package io.github.loadup.modules.upms.infrastructure.security.datascope;

/*-
 * #%L
 * Loadup Modules UPMS Infrastructure Layer
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
