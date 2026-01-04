package com.github.loadup.components.database.repository;

/*-
 * #%L
 * loadup-components-database
 * %%
 * Copyright (C) 2022 - 2026 loadup_cloud
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Skip Tenant Filter annotation
 *
 * <p>Mark a query method with this annotation to skip automatic tenant_id and deleted filtering.
 * Useful for admin queries or cross-tenant operations.
 *
 * <p>Example:
 *
 * <pre>
 * &#64;SkipTenantFilter
 * &#64;Query("SELECT * FROM upms_user")
 * List&lt;User&gt; findAllUsersAcrossAllTenants();
 * </pre>
 *
 * @author LoadUp Framework
 * @since 1.0.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkipTenantFilter {

  /** Skip tenant_id filter */
  boolean skipTenant() default true;

  /** Skip deleted filter */
  boolean skipDeleted() default true;
}
